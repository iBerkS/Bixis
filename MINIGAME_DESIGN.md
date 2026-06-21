# Bixis Minigame Sistemi — v1.0.1 Tasarım & Mimari Dokümanı

> **Durum: v1.0.1 implement edildi.** Bu doküman hem orijinal beyin
> fırtınasını hem de implementasyon sırasında netleşen/değişen
> kararları yansıtır. Tüm diyagramlar Mermaid formatında — GitHub
> bunları otomatik render eder, ek bir araç gerekmez.

---

## 1. Genel Bakış

Lucky Block race formatı (4 kişi yarışır → şans bloklarından TL
toplar → shop'tan item alır → arenada dövüşür) bir minigame state
machine'e bağlandı. Tekrarlayan teknik iş (geri sayım, ışınlama, kit
dağıtımı, süre takibi, sonuç hesaplama) otomatikleşti. Yaratıcı/değişken
kısımlar (boss seçimi, isimlendirme, PVP'nin kazananı) bilinçli olarak
manuel bırakıldı.

---

## 2. Mimari Genel Bakış

Dört yönetici sınıf, ortak bir `GameStateManager` etrafında çalışır.
Her biri kendi fazından sorumlu, config dosyalarını okur/yazar:

```mermaid
graph LR
    subgraph Managers["Yöneticiler"]
        GSM[GameStateManager]
        LM[LobbyManager]
        RM[RaceManager]
        AM[ArenaManager]
    end

    subgraph Config["config/"]
        RS[bixis-race-spawns.json]
        AS[bixis-arena-spawns.json]
        CP[bixis-checkpoints.json]
        RT[bixis-race-settings.json]
        MAPS["maps/&lt;isim&gt;/"]
    end

    subgraph Scoreboard["Vanilla Scoreboard Teams"]
        T1[team1 - Kırmızı]
        T2[team2 - Yeşil]
        T3[team3 - Sarı]
        T4[team4 - Mavi]
    end

    GSM --- LM
    GSM --- RM
    GSM --- AM

    LM --> Scoreboard
    LM -.set/list/tp.-> RS
    LM -.set/list/tp.-> AS
    RM -.kullanır.-> CP
    RM -.okur.-> RT
    AM -.kullanır.-> AS
    AM -.okur.-> RT
    MAPS -.loadmap ile kopyalanır.-> RS
    MAPS -.loadmap ile kopyalanır.-> AS
    MAPS -.loadmap ile kopyalanır.-> CP
```

- **GameStateManager** — tek doğruluk kaynağı, mevcut `GameState`'i tutar
- **LobbyManager** — Lobi + Geri Sayım fazları, takım/ready/sidebar
- **RaceManager** — Yarış fazı, checkpoint/ölüm/bitiş çizgisi/sonuç
- **ArenaManager** — Hazırlık 2 + Kapışma + Sonuç fazları

---

## 3. Oyun Durumları (State Machine)

7 durum, doğrusal bir döngü oluşturur:

```mermaid
flowchart LR
    LOBI([Lobi]) -->|"/bixis basla<br/>(tüm takımlar hazır)"| GS[Geri Sayım]
    GS -->|"3-2-1, survival,<br/>kit"| YARIS[Yarış]
    YARIS -->|"süre dolar VEYA<br/>herkes bitirir"| H1[Hazırlık 1]
    H1 -->|"/bixis arenaya_gec"| H2[Hazırlık 2]
    H2 -->|"/bixis kapisma_basla"| KAP[Kapışma]
    KAP -->|"süre dolar"| SONUC[Sonuç]
    SONUC -->|"/bixis sifirla"| LOBI
```

| Durum | Giriş tetikleyici | Çıkış tetikleyici | Süre sınırı |
|---|---|---|---|
| **Lobi** | Sunucu açılışı / `/bixis sifirla` | Tüm dolu takımlar hazır + admin `/bixis basla` | Yok |
| **Geri Sayım** | `/bixis basla` | Kit dağıtımı tamamlanınca otomatik | ~5 sn (sabit) |
| **Yarış** | Geri sayım bitince otomatik | Süre dolunca OTOMATİK, ya da tüm dolu takımların tüm üyeleri bitirince ERKEN | `set racetime` ile ayarlanabilir (varsayılan 15 dk) |
| **Hazırlık 1** | Yarış bitince otomatik | Admin `/bixis arenaya_gec` | Yok |
| **Hazırlık 2** | `/bixis arenaya_gec` | Admin `/bixis kapisma_basla` | Yok |
| **Kapışma** | `/bixis kapisma_basla` | Süre dolunca otomatik | `set pvptime` ile ayarlanabilir (varsayılan 3 dk) |
| **Sonuç** | Kapışma süresi dolunca otomatik | Admin `/bixis sifirla` | Yok |

---

## 4. Komut Mimarisi

```mermaid
flowchart TD
    ROOT["/bixis"] --> PLAYER[Oyuncu Komutları]
    ROOT --> FLOW[Akış Kontrol]
    ROOT --> ADMIN[Admin Harita Kurulumu]
    ROOT --> EVENTS[Lucky Eventler]
    ROOT --> INFO[Bilgi]

    PLAYER --> P1["takimsec, hazir, finish"]
    FLOW --> F1["basla, arenaya_gec,<br/>kapisma_basla, sifirla"]
    ADMIN --> A1["set, list, remove,<br/>reset, tp, loadmap"]
    EVENTS --> E1["luckyevents vergizamani"]
    INFO --> I1["durum, help"]
```

Tam liste için Bölüm 6'ya bakınız.

---

## 5. Faz Detayları

### 5.1 Lobi

Oyuncular `/bixis takimsec <1-4>` ile takım seçer, `/bixis hazir <1-4>`
ile hazır olduklarını bildirir. Takım üyeliği her değiştiğinde o
takımın ready flag'i otomatik `false`'a döner.

**Command block uyumluluğu:** Komutlar `execute as <selector> run ...`
kalıbıyla çağrılmalı — direkt `/bixis takimsec 1` yazmak command
block'u "oyuncu" olarak değil "command block entity"si olarak
çalıştırır ve reddedilir. Doğru kullanım, oyuncuyu konuma göre
filtreleyerek:

```mermaid
sequenceDiagram
    actor Oyuncu
    participant CB as Komut Bloğu
    participant LM as LobbyManager

    Oyuncu->>CB: Takım plakasına basar
    CB->>LM: execute as @p[distance=..2]<br/>run bixis takimsec 1
    LM->>LM: GameState == LOBI mi? Evet
    LM->>LM: Takıma ekle, ready flag sıfırla
    LM-->>Oyuncu: "1. takıma girdin!" + ses

    Oyuncu->>CB: Hazır plakasına basar
    CB->>LM: execute as @p[distance=..2]<br/>run bixis hazir 1
    LM->>LM: readyFlags[1] = true
    LM-->>Oyuncu: "Takım 1 Hazır!" (broadcast)
    LM->>LM: Tüm dolu takımlar hazır mı?
```

`distance=..2` filtresi, komut bloğuna gerçekten yaklaşmış olan
oyuncuyu garanti eder — birden fazla kişi aynı anda yakın dururlarsa
karışıklık önlenir.

**Takım renkleri:**

| Takım | Renk | Format kodu |
|---|---|---|
| 1 | Kırmızı | `&c` |
| 2 | Yeşil | `&a` |
| 3 | Sarı | `&e` |
| 4 | Mavi | `&9` |

**Hata mesajları:** `&4` (Dark Red) + ⚠ ile takım renklerinden ayrılır,
böylece Takım 1'in `&c` rengiyle karışmaz.

**Sidebar:** Lobi fazındayken canlı güncellenen bir scoreboard, her
takımın oyuncu sayısını ve hazır durumunu (✔/✘ ikonları) gösterir.

### 5.2 Geri Sayım

```mermaid
sequenceDiagram
    participant Admin
    participant LM as LobbyManager
    participant Oyuncular

    Admin->>LM: /bixis basla
    LM->>Oyuncular: Title "3" (ses, pitch 0.8)
    LM->>Oyuncular: Title "2" (ses, pitch 1.0)
    LM->>Oyuncular: Title "1" (ses, pitch 1.2)
    LM->>Oyuncular: Race spawn'a ışınla
    LM->>Oyuncular: Gamemode -> Survival
    LM->>Oyuncular: Kit ver (kazma, biftek, taş)
    LM->>Oyuncular: "YARIŞ BAŞLADI!" + Pling
    LM->>LM: GameState -> YARIS
```

**Kit içeriği:** 1x demir kazma, 10x pişmiş biftek, 32x taş.

### 5.3 Yarış

```mermaid
flowchart TD
    Y[Yarış fazı] --> D1[Oyuncu ölür]
    D1 --> D2[Son checkpoint'e ışınlanır]

    Y --> F1[Pressure plate'e basılır]
    F1 --> F2{Daha önce bitirdi mi?}
    F2 -->|Hayır| F3[Süre + ölüm kaydedilir]
    F2 -->|Evet| F4[Yoksayılır]
    F3 --> F5[RaceManager'a yazılır]

    Y -->|"süre dolar VEYA<br/>herkes bitirir"| R["Yarış Sonuçları<br/>chat'e yazdırılır"]
```

- `keepInventory` gamerule'a **mod dokunmaz** — manuel olarak true
  tutulması bekleniyor, oyunun tamamında geçerli.
- Bitiş çizgisi pressure plate altına komut bloğu ile bağlanır:
  `execute as @p[distance=..1] run bixis finish`
- Erken bitiş: süre dolmadan tüm dolu takımların tüm üyeleri finish
  ederse Hazırlık 1'e hemen geçilir.

**Chat formatı (Sonuç fazına geçişte):**
```
─── Yarış Sonuçları ───
[Oyuncu] - mm:ss.ms - N Ölüm
[Oyuncu] - Bitirmedi - N Ölüm
```

### 5.4 Hazırlık 1 — Race Map

Süre sınırı yok. Shop (Villa Hakan), envanter düzeni, mola.

### 5.5 Hazırlık 2 — Arena

### 5.6 Kapışma — PVP

### 5.7 Sonuç

```mermaid
sequenceDiagram
    participant Admin
    participant AM as ArenaManager
    participant Oyuncular

    Admin->>AM: /bixis arenaya_gec
    AM->>Oyuncular: "Hazırlık dönemi bitti,<br/>arenaya ışınlanıyorsunuz."
    AM->>Oyuncular: Arena spawn'a ışınla
    Note over Admin,Oyuncular: Boss avı (manuel)<br/>Ölüm = arena spawn'a respawn

    Admin->>AM: /bixis kapisma_basla
    AM->>Oyuncular: Arena spawn'a TEKRAR ışınla
    AM->>Oyuncular: Title "3" (konum kilitli)
    AM->>Oyuncular: Title "2" (konum kilitli)
    AM->>Oyuncular: Title "1" (konum kilitli)
    AM->>Oyuncular: "BAŞLA!" + Pling
    AM->>AM: Kronometre başlar, respawn serbest
    Note over Admin,Oyuncular: PVP, kill sadece oyuncu-oyuncu<br/>Ölüm = arena spawn'a respawn

    AM->>Oyuncular: Süre dolar
    AM->>Oyuncular: "KAPIŞMA BİTTİ!" + Bell
    AM->>Oyuncular: Kapışma Sonuçları (chat)
```

**Hazırlık 2 ve Kapışma — tek arena:** Boss arena ile PVP arena
**aynı fiziksel mekan**. Her takım kendi köşesinde başlar, sonra
toplanır. Ayrı bir "boss arena" / "PVP arena" ayrımı yok.

**Konum kilitleme (anti-bypass):** Kapışma geri sayımında Slowness
efekti **kullanılmaz** — koşu+zıplama ile bypass edilebiliyordu.
Bunun yerine her tick oyuncu countdown başındaki pozisyona zorla
sabitlenir (teleport), sadece bakış açısı serbest kalır.

**Respawn (Hazırlık 2 ve Kapışma ortak):** Ölen oyuncu vanilla
respawn yerine kendi takımının arena spawn noktasına ışınlanır.

**Kill sayacı:** Sadece oyuncu-oyuncu öldürmeleri sayılır — mob
öldürmek kill'e katkı sağlamaz (`event.getEntity() instanceof
ServerPlayer` VE `event.getSource().getEntity() instanceof
ServerPlayer` ikisi de kontrol edilir). Death sayacı her ölümde artar
(mobdan ölse bile).

**Chat formatı:**
```
─── Kapışma Sonuçları ───
[Oyuncu]: N Öldürme / N Ölüm
```

Kazanan otomatik hesaplanmaz — format açık uçlu, izleyici yorumlarda
seçer.

---

## 6. Komut Referansı

### 6.1 Oyuncu komutları

| Komut | Açıklama | Hangi fazda |
|---|---|---|
| `/bixis takimsec <1-4>` | Takıma atar, eski takımdan çıkarır, her iki takımın ready flag'ini sıfırlar | Lobi |
| `/bixis hazir <1-4>` | Ready flag'i true yapar, takım renginde broadcast | Lobi |
| `/bixis finish` | Bitiş çizgisinde tetiklenir (pressure plate + command block) | Yarış |

### 6.2 Akış kontrol komutları

| Komut | Açıklama | Ön koşul |
|---|---|---|
| `/bixis basla` | Lobi → Geri Sayım | Tüm dolu takımlar hazır olmalı |
| `/bixis arenaya_gec` | Hazırlık 1 → Hazırlık 2, ışınlama | Yok |
| `/bixis kapisma_basla` | Hazırlık 2 → Kapışma, konum kilitli countdown | Yok |
| `/bixis sifirla` | Her şeyi Lobi durumuna resetler | Herhangi bir durumda |

> **Not:** Orijinal tasarımda yer alan genel amaçlı `/bixis bitir`
> (mevcut fazı zorla bitirme) implement edilmedi — yerini daha
> spesifik geçiş komutları (`arenaya_gec`, `kapisma_basla`,
> `sifirla`) aldı.

### 6.3 Admin harita kurulum komutları

| Komut | Açıklama |
|---|---|
| `/bixis admin set race <1-4>` | Yarış başlangıç noktasını (konum+yön+dimension) kaydeder |
| `/bixis admin set arena <1-4>` | Arena/PVP spawn noktasını kaydeder |
| `/bixis admin set checkpoint <1-4>` | Yeni checkpoint ekler (otomatik sıra numarası) |
| `/bixis admin set racetime <dakika>` | Yarış fazı süresini ayarlar (varsayılan 15) |
| `/bixis admin set pvptime <dakika>` | Kapışma fazı süresini ayarlar (varsayılan 3) |
| `/bixis admin list race \| arena \| checkpoint <1-4>` | Kayıtlı noktaları chat'e döker |
| `/bixis admin remove checkpoint <1-4> <sira>` | Checkpoint siler, sıra numaralarını kaydırır |
| `/bixis admin reset all` | Tüm harita kayıtlarını siler |
| `/bixis admin tp race \| arena \| checkpoint <1-4> [sira]` | Test amaçlı ışınlanma |
| `/bixis admin loadmap <isim>` | `config/maps/<isim>/` klasöründen aktif config'e kopyalar |

### 6.4 Lucky event komutları

| Komut | Açıklama |
|---|---|
| `/bixis luckyevents vergizamani` | Tüm oyunculardan 5-25 TL siler, title + Bell sesi |

> Genişletilebilir yapıda kuruldu — yeni event komutları (örn.
> `parayagmuru`) aynı `luckyevents` altına eklenebilir.
> **Önemli:** Bu, eski `/bixis vergizamani`'nin yerini aldı.
> Addon'lardaki `drops.txt` dosyaları güncellenmeli (Mehmet Şimşek
> teması güncellendi, diğer addon'lar kontrol edilmeli).

### 6.5 Bilgi komutları

| Komut | Açıklama |
|---|---|
| `/bixis durum` | Mevcut GameState'i chat'e yazar |
| `/bixis help` | Tüm komutları kategorilere ayırarak listeler |
| `/bixis ping` | Mod aktif mi test eder (v1.0.0'dan kalma) |

---

## 7. Veri Mimarisi

| Veri | Saklama yöntemi |
|---|---|
| Takım üyeliği, renk, isim | Vanilla Scoreboard Team API |
| Ready flag (takım başına) | `GameStateManager`/`LobbyManager` içinde bellekte |
| Race/arena/checkpoint koordinatları | `config/bixis-*.json` (her video için yeniden doldurulur) |
| Yarış/Kapışma süreleri | `config/bixis-race-settings.json` |
| Yarış süresi + ölüm sayısı | `RaceManager` içinde bellekte, Sonuç fazında chat'e yazdırılır |
| Mevcut oyun durumu | `GameStateManager`, bellekte (kalıcı değil, sunucu yeniden başlayınca sıfırlanır) |

Mimari diyagram için bkz. Bölüm 2.

---

## 8. Kararlar

Tüm açık kararlar netleşti ve implement edildi:

- **Ready flag reset:** Takım üyeliği değişince ilgili takımın ready
  flag'i otomatik `false`'a döner.
- **Lobi sidebar:** Canlı güncellenen tablo, ✔/✘ ikonlarıyla.
- **Kapışma kill/death:** Sadece oyuncu-oyuncu öldürmeleri sayılır.
- **Takım renkleri:** Kırmızı/Yeşil/Sarı/Mavi (`&c`/`&a`/`&e`/`&9`).
- **Boss seçimi/isimlendirmesi:** Tamamen manuel kalır.
- **Kapışma kazananı:** Otomatik hesaplanmaz, izleyici seçer.
- **Tek arena:** Boss avı ve PVP aynı fiziksel mekanda.
- **Konum kilitleme:** Slowness değil, zorla teleport (anti-bypass).

---

## 9. Bilinen Sınırlamalar / Sonraki Adımlar

**Doğrulanması gereken:**
- `/bixis admin loadmap` çok oyunculu/gerçek senaryoda tam test
  edilmedi (tek kişilik testte mantık doğrulandı, dosya kopyalama
  akışı net değil).

**1.0.1 sonrası (1.0.2 adayı):**
- `/bixis help` çıktısına her komutun yanına kısa açıklama eklenmesi
- Sound tablosunun geri kalan kısımlarının (gerekirse) ince ayarı
