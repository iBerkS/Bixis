# Changelog

## v1.0.1 - Minigame Sistemi

### Bixis Mod

#### Minigame — Durum Makinesi
Sunucu taraflı bir oyun akış sistemi eklendi. Oyun yedi fazdan oluşur:
**Lobi → Geri Sayım → Yarış → Hazırlık 1 → Hazırlık 2 → Kapışma → Sonuç**

Her faz, önceki fazın tamamlanmasıyla otomatik ya da admin komutuyla tetiklenir.

#### Minigame — Lobi Fazı
- **Takım Seçimi** — Oyuncular `/bixis takimsec <1-4>` ile dört takımdan birine katılır. Takımlar kırmızı, yeşil, sarı ve mavi renkleriyle gösterilir.
- **Hazır Bildirimi** — `/bixis hazir <1-4>` komutuyla takım hazır olarak işaretlenir. Dolu olan tüm takımlar hazır olduğunda `/bixis basla` kullanılabilir.
- **Lobi Scoreboard** — Ekranın sağ tarafında her takımın oyuncu sayısı ve hazır durumunu gösteren canlı bir tablo görünür.

#### Minigame — Geri Sayım Fazı
- `/bixis basla` komutundan sonra 3-2-1 geri sayım başlar.
- Geri sayım bitince her oyuncu kendi takımının yarış başlangıç noktasına ışınlanır, Survival moduna alınır ve başlangıç kiti (demir kazma, biftek ve kırıktaş) otomatik verilir.

#### Minigame — Yarış Fazı
- **Checkpoint Sistemi** — Parkurda belirlenen kontrol noktaları; oyuncu ölünce son geçtiği checkpoint'e ışınlanır.
- **Bitiş Çizgisi** — Parkurun sonundaki basınç plakası `/bixis finish` komutunu tetikler; bitiş süresi ve anlık sıra ekranda gösterilir, sunucuya duyurulur.
- **Ölüm Sayacı** — Her oyuncunun kaç kez öldüğü kaydedilir.
- **Yarış Scoreboard** — Ekranın sağ tarafında kalan süre ve takım bazlı bitiş sayıları gösterilir.
- **Süre Dolunca** — 15 dakika (ayarlanabilir) sonunda yarış otomatik biter; sonuçlar chat'e yazdırılır ve Hazırlık 1 fazına geçilir.

#### Minigame — Hazırlık 1 Fazı
- Yarış sona erdikten sonra oyuncular race haritasında bekler: shop'tan alışveriş, envanter düzenleme.
- Admin `/bixis arenaya_gec` komutunu çalıştırınca herkes arena haritasına ışınlanır.

#### Minigame — Hazırlık 2 Fazı
- Oyuncular arenadadır; admin boss'ları manuel olarak belirleyip spawnlar.
- Bu fazda ölen oyuncu kendi takımının arena spawn noktasına ışınlanır.

#### Minigame — Kapışma (PVP) Fazı
- `/bixis kapisma_basla` komutuyla 3-2-1 geri sayım başlar; geri sayım sırasında oyuncuların pozisyonu kilitlenir.
- Geri sayım bitince serbest PVP başlar; kill/death sayaçları tutulur (sadece oyuncu-oyuncu öldürmeleri kill sayar).
- Kapışma Scoreboard — kalan süre, her oyuncunun kill/death sayısı anlık olarak görünür.
- Süre dolunca kapışma biter; yarış ve kapışma sonuçları chat'e yazdırılır.

#### Minigame — Sonuç Fazı
- Kapışma sona erdikten sonra chat'e iki sonuç bloğu gönderilir:
  - **Yarış Sonuçları** — bitiş sırası, süre ve ölüm sayısı
  - **Kapışma Sonuçları** — kill / ölüm sayısı
- `/bixis sifirla` ile her şey Lobi'ye döner, yeni oturum başlayabilir.

#### Admin Harita Kurulum Komutları
Video çekiminden önce haritayı bir kez ayarlamak için kullanılır; config dosyalarına kaydedilir.

- `/bixis admin set race <1-4>` — Yarış başlangıç noktasını kaydeder
- `/bixis admin set arena <1-4>` — Arena spawn noktasını kaydeder
- `/bixis admin set checkpoint <1-4>` — Takımın checkpoint listesine yeni nokta ekler
- `/bixis admin set racetime <dakika>` — Yarış süresini ayarlar (varsayılan: 15 dk)
- `/bixis admin set pvptime <dakika>` — Kapışma süresini ayarlar (varsayılan: 3 dk)
- `/bixis admin list race/arena/checkpoint` — Kayıtlı noktaları listeler
- `/bixis admin remove checkpoint <takım> <sıra>` — Checkpoint siler
- `/bixis admin reset all` — Tüm harita kayıtlarını temizler
- `/bixis admin tp race/arena/checkpoint` — Admin'i kayıtlı noktaya ışınlar
- `/bixis admin loadmap <isim>` — `config/maps/<isim>/` klasöründen hazır harita config'ini yükler

#### Diğer Yeni Komutlar
- `/bixis help` — Tüm komutları kategorili olarak listeler
- `/bixis durum` — Mevcut oyun fazını gösterir
- `/bixis luckyevents vergizamani` — Lucky Block'tan tetiklenen Vergi Zamanı eventi (eski `/bixis vergizamani` kaldırıldı)
- `/bixis finish` — Bitiş çizgisindeki command block tarafından tetiklenir

#### Teknik
- Command block desteği: tüm komutlar command block üzerinden (`execute as <oyuncu> run ...` ile) çalışacak şekilde düzenlendi.
- Harita config dosyaları: `config/bixis-race-spawns.json`, `config/bixis-arena-spawns.json`, `config/bixis-checkpoints.json`, `config/bixis-race-settings.json`

---

## v1.0.0 - İlk Sürüm

### Bixis Mod

#### Item'lar
- **Türk Lirası** — Oyunun para birimi. NPC'lerle alışveriş ve döviz işlemlerinde kullanılır.
- **Fenerbahçe Kılıcı** — Yüksek hasarlı özel kılıç.
- **Fenerbahçe Forması** — Giyince Strength I efekti veren özel göğüs zırhı.
- **TC Pasaportu** — Ölüm anında Totem of Undying gibi devreye girer; Speed II, Jump Boost II ve Regeneration I verir.
- **Faiz Kalkanı** — Elde tutulunca her 20 saniyede 1 kalp Absorption biriktirir, maksimum 5 kalp.
- **Mutlak Butlan** — Elde tutulduğu sürece Resistance II verir.
- **Puro** — Tüketince duman efekti çıkar.
- **Fent** — Tüketince Poison II + Nausea II verir.

#### Silahlar
- **Hançer** (Demir / Elmas / Netherite) — Çok hızlı saldırı hızlı kısa kılıç.
- **Mızrak** (Demir / Elmas / Netherite) — Uzun erişimli silah; sağ tıkla fırlatılır, geri alınabilir.
- **Yatağan** (Demir / Elmas / Netherite) — Her vuruşta %8 ihtimalle Kanama II efekti uygular.
- **Gaddare** (Demir / Elmas / Netherite) — Yavaş ama ağır kılıç; Slowness uygular, düşük canlı hedeflere iki katı hasar verir.
- **Ateş Asası** — Sağ tıkla 6 blok çevresindeki düşmanları tutuşturur.
- **Desert Eagle** — 7 şarjörlü yarı otomatik tabanca.
- **AWP** — 5 şarjörlü yüksek hasarlı keskin nişancı tüfeği.
- **M4** — 30 şarjörlü tam otomatik tüfek.
- **Mermi** — Desert Eagle, AWP ve M4 için ortak mermi.

#### Mob Efektleri
- **Kanama** — Zamanla hasar veren, zehirden farklı olarak öldürebilen özel efekt.

#### Mob'lar
- **Hırt** — Saldırgan mob; yerden item toplar, %25 ihtimalle demir hançerle doğar. 4 farklı görünüm varyantı.
- **Recep İvedi** — Nötr mob; her birkaç saniyede bir etrafındaki blokları kırar ve rastgele varlıklara yumruk atar.
- **Kemal Darkılıçoğlu** — Tamamen pasif; bir sandalyede oturur. Öldürülünce Mutlak Butlan düşürür.
- **George Floid** — Pasif mob; sonsuz Kanama efektiyle doğar. Öldürülünce Fent düşürür.
- **Abugat** — Pasif mob; dans eder, çeşitli sesler çıkarır. Öldürülünce Desert Eagle, mermi ve TL düşürür.
- **Türk Polisi** — Düşman mob (50 HP); üç state'i vardır: normal saldırı, rüşvet (TL düşürülünce pasifleşir) ve mob dövüşü. Netherite kılıç + kalkan taşır.
- **Fenerbahçe / Galatasaray / Beşiktaş / Trabzonspor Holiganı** — Kendi taraftarlarıyla barışık, rakip taraftarlara saldıran holiganlar. Her 60 saniyede ambient ses çıkarır.

#### NPC'ler
- **Rahim Koç (Döviz Bürosu)** — Demir, altın, elmas, netherite vb. malzemeleri Türk Lirası'na çevirir.
- **Villa Hakan (Shop)** — Türk Lirası karşılığında Fenerbahçe Forması, Desert Eagle, AWP, M4, mermi, Fenerbahçe Kılıcı, TC Pasaportu ve Lucky Potion satar.

#### Admin Komutları
- `/bixis ping` — Modun aktif olduğunu doğrular.
- `/bixis vergizamani` — Sunucudaki tüm oyunculardan 5–25 adet Türk Lirası tahsil eder; tüm oyunculara başlık ve chat bildirimi gönderir, wither spawn sesi çalar.

#### Teknik
- Döviz oranları `config/bixis-rates.json` dosyasından okunur, oyun yeniden başlatılmadan güncellenebilir.

---

### Bixis Addon — Kadıköy Boğası Lucky Block

Fenerbahçe temalı lucky block. Lacivert renk, animasyonlu overlay.

**Lucky Eventler (öne çıkanlar):**
- Fenerbahçe Holiganı sürüsü, Aziz Yıldırım (Iron Golem boss), Giant Zombie
- Tüm Bixis silahları (hançer / mızrak / yatağan / gaddare, demir → netherite)
- TL yağmuru, Abugat spawn, Beacon, büyülü elmas/netherite kılıçlar
- Enchanting Setup ve Lucky Altar yapıları

**Unlucky Eventler (öne çıkanlar):**
- Ali Koç (tam elmas zırhlı zombie boss), Muslera (netherite zırhlı), İcardi, Osimhen
- Galatasaray / Beşiktaş / Trabzonspor Holigan sürüleri
- George Floid, Hırt sürüsü, Recep İvedi, Kemal Darkılıçoğlu
- Dev Slime ve Dev Magma Küpü, çeşitli patlama ve mob sürüsü eventleri
- Unlucky Altar yapıları

---

### Bixis Addon — Mehmet Şimşek Lucky Block

Ekonomi / maliye temalı lucky block. Yeşil renk.

**Lucky Eventler (öne çıkanlar):**
- **Faiz Kalkanı** — Absorption biriktiren özel item
- **Yeşil Pasaport** — TC Pasaportu + havai fişek
- **TOKİ Yapısı** — Oyuna özel TOKİ binası struktur
- Tüm Bixis silahları, TL yağmuru, Abugat, standart sandık ve kaynak eventleri

**Unlucky Eventler (öne çıkanlar):**
- **Vergi Zamanı** — Tüm oyunculardan TL tahsil edilir, ekrana kırmızı başlık ve wither sesi
- **Türk Polisi** — 1–4 Türk Polisi spawn olur
- George Floid, Hırt sürüsü, Recep İvedi, Dev Slime/Magma Küpü
