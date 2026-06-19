# Changelog

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
