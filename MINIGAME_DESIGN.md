# Bixis Minigame Sistemi — v1.0.1 Tasarım Dokümanı

> Bu doküman, v1.0.0'dan sonra üzerinde konuşulan minigame sistemi için
> beyin fırtınasının tam dökümüdür. Implementasyona geçmeden önce referans
> olarak kullanılması, Claude Code'a verilecek promptların buradan
> türetilmesi amaçlanmıştır.

---

## 1. Genel Bakış

Mevcut Lucky Block race formatı (4 kişi yarışır → şans bloklarından TL
toplar → shop'tan item alır → arenada dövüşür) artık server-side bir
**minigame state machine**'e bağlanıyor. Amaç, video çekim akışını manuel
müdahale ihtiyacını azaltarak otomatikleştirmek — ama **eğlence/içerik
değeri taşıyan kısımları** (boss seçimi, isimlendirme, PVP'nin kazananı)
bilinçli olarak manuel bırakmak.

**Temel prensip:** Tekrarlayan teknik iş (geri sayım, ışınlama, kit
dağıtımı, süre takibi, scoreboard) otomatikleşir. Yaratıcı/değişken
kısımlar (boss lineup, isimlendirme, PVP kazananı) manuel kalır.

---

## 2. Oyun Durumları (State Machine)

7 durum, doğrusal bir döngü oluşturur:

```
Lobi → Geri Sayım → Yarış → Hazırlık 1 → Hazırlık 2 → Kapışma → Sonuç → (Lobi)
```

| Durum | Giriş tetikleyici | Çıkış tetikleyici | Süre sınırı |
|---|---|---|---|
| **Lobi** | Sunucu açılışı / `/bixis sifirla` | Tüm dolu takımlar hazır + admin `/bixis basla` | Yok |
| **Geri Sayım** | `/bixis basla` | Kit dağıtımı tamamlanınca otomatik | ~5 sn (sabit) |
| **Yarış** | Geri sayım bitince otomatik | 15 dk dolunca otomatik | 15 dakika |
| **Hazırlık 1** | Yarış süresi dolunca otomatik | Admin `/bixis arenaya_gec` | Yok |
| **Hazırlık 2** | `/bixis arenaya_gec` | Admin `/bixis kapisma_basla` | Yok |
| **Kapışma** | `/bixis kapisma_basla` | Süre dolunca otomatik | Ayarlanabilir (örn. 3 dk) |
| **Sonuç** | Kapışma süresi dolunca otomatik | Admin `/bixis sifirla` | Yok |

`GameState` bir enum olarak tek bir server-side singleton'da tutulur.
Sunucu yeniden başlayınca sıfırlanması sorun değil — her video çekimi
zaten taze bir sunucu oturumu.

---

## 3. Faz Detayları

### 3.1 Lobi

Oyuncular `/bixis takimsec <1-4>` ile takım seçer, `/bixis hazir <1-4>`
ile hazır olduklarını bildirir. **Dolu** olan tüm takımlar (en az 1
oyuncusu olan) hazır olunca `/bixis basla` komutunun kilidi açılır.

Takım depolama Minecraft'ın kendi **Scoreboard Team** sistemiyle yapılır
— renk, isim, oyuncu listesi bedavaya gelir.

Komut akışı detayı için bkz. **Diyagram 2** (Lobi komut akışı).

### 3.2 Geri Sayım

`/bixis basla` çalışınca:

```
1. Title: "3" → 1 saniye bekle
2. Title: "2" → 1 saniye bekle
3. Title: "1" → 1 saniye bekle
4. Her oyuncu kendi takımının race başlangıç noktasına ışınlanır
5. Milisaniye farkla /bixis kit @a tetiklenir (herkes kitini alır)
6. playSound + title: "YARIŞ BAŞLADI! Bol şans!"
7. Yarış fazına otomatik geçiş, 15 dakikalık sayaç başlar
```

### 3.3 Yarış

15 dakikalık ana faz. İki bağımsız sistem paralel çalışır:

**Ölüm & checkpoint:** Oyuncu ölünce (lav, TNT, düşme vb.) parkurun
başına değil, **son geçtiği checkpoint'e** ışınlanır. `keepInventory`
true olduğu için eşya kaybı yaşanmaz — amaç cezalandırmak değil,
videonun tekrar tekrar "baştan yürüme" sahneleriyle dolmasını önlemek.

**Bitiş çizgisi & scoreboard:** Parkurun sonunda bir pressure plate var.
Basılınca arka planda `/bixis finish @s` tetiklenir:

```
1. Oyuncu daha önce bitirdi mi kontrol edilir
   - Evet → hiçbir şey olmaz (tekrar tetiklenmesin)
   - Hayır → devam
2. Yarış başlangıcından bu yana geçen süre kaydedilir
3. O ana kadarki ölüm sayısı kaydedilir
4. Scoreboard güncellenir (otomatik sıralanır)
5. Oyuncuya title: "Bitirdin! N. sıradasın"
```

Detaylı akış için bkz. **Diyagram 3** (Yarış fazı mekanikleri).

15 dakika dolunca, henüz bitirmemiş oyuncular için de mevcut durumla
(en son checkpoint, ölüm sayısı) bir kayıt düşülür, faz otomatik olarak
Hazırlık 1'e geçer.

### 3.4 Hazırlık 1 — Race Map

Süre sınırı yok. Oyuncular hâlâ race haritasında — shop'tan (Villa
Hakan) item alımı, envanter düzenleme, sohbet. Video çekimi sırasında
mola/planlama amaçlı kullanılan bir faz.

Admin `/bixis arenaya_gec` çalıştırınca herkes arena haritasına geçer.

### 3.5 Hazırlık 2 — Arena

Süre sınırı yok, **tamamen manuel**. Bu fazın değeri otomasyonda değil,
içerik yaratıcısının seçimlerinde:

- Hangi 3 boss kullanılacak (Cataclysm boss'larından)
- Bu boss'lara hangi temalı isimler verilecek (örn. "Yakışıklı Güvenlik",
  "Eren Karayılan", "Nişanlı Rabia")
- Hangi sırayla öldürülecekleri

Spawn egg'ler manuel alınır, boss'lar birlikte öldürülür. Bitince admin
`/bixis kapisma_basla` ile Kapışma fazına geçer.

> **Not:** Otomatik boss rotasyonu (rastgele havuzdan seçim, sıralı
> zincir vb.) bilinçli olarak tercih edilmedi — çünkü asıl mizah/içerik
> değeri temaya özel isimlendirmeden geliyor, otomasyon bunu öldürür.

### 3.6 Kapışma — PVP

`/bixis kapisma_basla` ile:

```
1. Kronometre başlar (varsayılan süre ayarlanabilir, örn. 3 dakika)
2. Respawn serbest bırakılır
3. Serbest PVP başlar (free-for-all)
```

**Kazanan otomatik hesaplanmıyor** — format bilinçli olarak açık uçlu
bırakılıyor, izleyici yorumlarda "kazananı" seçiyor. Bu, ilk videodaki
formatın korunması için bilinçli bir tasarım kararı.

> **Öneri (netleşmedi):** Otomatik kazanan belirlemese bile, eğlence
> katmanı olarak bir "en çok öldüren" kill-counter scoreboard'a
> eklenebilir.

### 3.7 Sonuç

Kapışma süresi dolunca otomatik tetiklenir. Yarış fazından toplanan
scoreboard verisi gösterilir:

```
1. Dragonite1606 - 12:00.97 - 0 Deaths
2. ...
```

Admin `/bixis sifirla` ile her şey Lobi'ye döner, yeni bir oturum
başlayabilir.

---

## 4. Komut Referansı

### 4.1 Oyuncu komutları

| Komut | Açıklama | Hangi fazda çalışır |
|---|---|---|
| `/bixis takimsec <1-4>` | Oyuncuyu belirtilen takıma atar, eski takımdan çıkarır | Lobi |
| `/bixis hazir <1-4>` | Oyuncunun takımı için ready flag'ini true yapar | Lobi |

### 4.2 Otomatik / sistem tetikleyicileri

| Tetikleyici | Açıklama |
|---|---|
| Pressure plate (bitiş çizgisi) | Arka planda `/bixis finish @s` çalıştırır, süre + ölüm kaydeder |
| Checkpoint (admin tarafından kurulu) | Ölüm sonrası respawn noktası olarak kullanılır |

### 4.3 Akış kontrol komutları (admin, manuel faz geçişleri)

| Komut | Açıklama | Ön koşul |
|---|---|---|
| `/bixis basla` | Lobi → Geri Sayım | Tüm dolu takımlar hazır olmalı |
| `/bixis arenaya_gec` | Hazırlık 1 → Hazırlık 2 | Yok, istenildiğinde |
| `/bixis kapisma_basla` | Hazırlık 2 → Kapışma, kronometre başlatır | Yok, istenildiğinde |
| `/bixis bitir` | Mevcut fazı zorla bitirir | Herhangi bir durumda |
| `/bixis sifirla` | Her şeyi Lobi durumuna resetler | Herhangi bir durumda |

### 4.4 Admin harita kurulum komutları

| Komut | Açıklama |
|---|---|
| `/bixis admin set race <1-4>` | O takımın yarış başlangıç noktasını (konum + yön) kaydeder |
| `/bixis admin set arena <1-4>` | O takımın arena/PVP spawn noktasını kaydeder |
| `/bixis admin set checkpoint <1-4> <1-M>` | O takımın M. checkpoint'ini kaydeder |
| `/bixis admin list race \| arena \| checkpoint <1-4>` | Kayıtlı noktaları chat'e döker |
| `/bixis admin remove checkpoint <1-4> <1-M>` | Belirli bir checkpoint kaydını siler |
| `/bixis admin reset all` | Tüm harita kayıtlarını siler |
| `/bixis admin tp race \| arena \| checkpoint <1-4> [M]` | Test amaçlı kayıtlı noktaya ışınlanır |
| `/bixis admin loadmap <isim>` | Farklı bir harita config dosyasını yükler |

### 4.5 Mevcut (v1.0.0) komutlar — değişmiyor

| Komut | Açıklama |
|---|---|
| `/bixis ping` | Mod aktif mi test eder |
| `/bixis vergizamani` | Tüm oyunculardan rastgele TL siler, title + ses efekti |

---

## 5. Veri Mimarisi

| Veri | Saklama yöntemi |
|---|---|
| Takım üyeliği, renk, isim | Vanilla Scoreboard Team API |
| Ready flag (takım başına) | `GameState` singleton içinde 4 elemanlı boolean array |
| Race/arena/checkpoint koordinatları | `config/bixis-arena.json` (her video için yeniden doldurulur) |
| Yarış süresi + ölüm sayısı | Scoreboard Objectives (`race_time`, `race_deaths`) |
| Mevcut oyun durumu | `GameState` enum, server-side singleton, bellekte (kalıcı değil) |

**`config/bixis-arena.json` taslak yapısı:**

```json
{
  "race": {
    "1": { "x": 120, "y": 64, "z": 88, "yaw": 90 },
    "2": { "...": "..." }
  },
  "arena": {
    "1": { "...": "..." }
  },
  "checkpoints": {
    "1": [ { "...": "..." }, { "...": "..." } ]
  }
}
```

Her video için farklı harita kurulacağı için, `/bixis admin loadmap
<isim>` ile tema bazlı ayrı dosyalar arasında geçiş yapılabilir (örn.
`bixis-arena-kadikoybogasi.json`).

---

## 6. Kararlar

Tüm açık kararlar netleşti:

- **Ready flag reset:** Bir takıma yeni oyuncu katılınca ya da biri
  ayrılınca o takımın ready flag'i otomatik `false`'a döner.
- **Lobi sidebar scoreboard:** Eklenecek. Canlı olarak güncellenen bir
  tablo, her takımın oyuncu sayısını ve hazır durumunu gösterir:
  ```
  LOBİ DURUMU
  Takım 1: 2 oyuncu - HAZIR
  Takım 2: 1 oyuncu - BEKLENİYOR
  Takım 3: 0 oyuncu - -
  Takım 4: 3 oyuncu - HAZIR
  ```
- **Kapışma kill/death counter:** Eklenecek. PVP fazında her oyuncunun
  kaç öldürdüğü ve kaç öldüğü görünür olacak (otomatik kazanan
  hesaplamaz, sadece istatistik amaçlı).
- **Takım renkleri:**

  | Takım | Renk | Format kodu |
  |---|---|---|
  | 1 | Kırmızı | `&c` |
  | 2 | Yeşil | `&a` |
  | 3 | Sarı | `&e` |
  | 4 | Koyu Mavi | `&9` |


**Önceden kesinleşen kararlar:**

- Hazırlık 2'deki boss seçimi/isimlendirmesi **tamamen manuel** kalacak,
  otomasyona bağlanmayacak.
- Kapışma'nın kazananı otomatik hesaplanmayacak, format açık uçlu
  kalacak (izleyici seçimi).
- Checkpoint sistemi + `keepInventory: true` kombinasyonu kullanılacak.

---

## 7. Teknik Implementasyon Notları

Claude Code'a yazdırırken faydalı olacak Forge event hook önerileri:

| İhtiyaç | Önerilen yaklaşım |
|---|---|
| Pressure plate tetikleme | Plaka altına command block, `/bixis finish @s` çağırır — vanilla redstone event dinlemeye gerek kalmaz |
| Checkpoint respawn | `LivingDeathEvent` veya `PlayerRespawnEvent` override, son checkpoint koordinatına yönlendirme |
| Geri sayım / yarış / kapışma süreleri | `ServerTickEvent`, `GameState` singleton içinde tick sayaçları |
| Takım bazlı ışınlama + kit dağıtımı | Aynı tick içinde sıralı `teleport` + `give` çağrıları (milisaniye farkı oyuncu gözünde fark edilmez) |

---

## 8. Sonraki Adımlar

1. Açık kararları (Bölüm 6) netleştir
2. `GameState` enum + singleton iskeletini yazdır
3. Lobi fazı komutlarını (`takimsec`, `hazir`, ready flag kontrolü) yazdır
4. Admin harita kurulum komutlarını yazdır
5. Geri sayım + yarış fazı mekaniklerini yazdır
6. Hazırlık + Kapışma + Sonuç fazlarını yazdır
7. Uçtan uca test (gerçek bir harita kurarak)