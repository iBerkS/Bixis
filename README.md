# 🇹🇷 Bixis Mod & Addon

**Bixis** YouTube kanalı için geliştirilen, tematik ve absürt bir Minecraft içerik paketidir. Forge modu ve Lucky Block addon'larından oluşur — Lucky Block race formatında çekilen videolar için özel olarak tasarlandı.

> Belirtmekte fayda var, bu ilk şahsi Minecraft mod denememdir. Yorumlarınız ve fikirleriniz üzerine inşa ediyorum:)

---

## 🎮 Bu Pakette Neler Var?

- **Özel para birimi:** Türk Lirası — lucky block'lardan düşer, NPC'lerde harcanır
- **NPC'ler:** Rahim Koç (döviz bürosu), Villa Hakan (silah dükkanı)
- **12+ özel silah:** Hançer, Mızrak, Yatağan, Gaddare — her biri demir/elmas/netherite varyantıyla
- **Ateşli silahlar:** Desert Eagle, AWP, M4 — şarjör ve reload sistemiyle
- **Özel mob'lar:** Hırt, Recep İvedi, George Floid, Abugat, Kemal Darkılıçoğlu, takım holiganları, Türk Polisi ve daha fazlası
- **Lucky Block temaları:** Kadıköy Boğası, Mehmet Şimşek (devam ediyor)
- **Rün itemler:** Mutlak Butlan, Faiz Kalkanı gibi pasif efekt veren özel eşyalar

---

## 📋 Gereksinimler

Bu modu çalıştırmak için aşağıdaki modların kurulu olması **zorunludur**:

| Mod | Versiyon |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| [Lucky Block](https://www.curseforge.com/minecraft/mc-mods/lucky-block) | 13.0 |
| [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) | 4.8.3 |
| [L_Ender's Cataclysm](https://www.curseforge.com/minecraft/mc-mods/cataclysm) | 3.31 |
| Lionfish API | 3.0 *(Cataclysm bağımlılığı)* |
| Curios API | 5.14.1 *(Cataclysm bağımlılığı)* |

---

## 🛠️ Kurulum

### 1. Bağımlılıkları indir

Yukarıdaki tabloda listelenen tüm modları CurseForge veya Modrinth üzerinden indir.

### 2. Mods klasörüne kopyala

İndirdiğin tüm `.jar` dosyalarını (Lucky Block, GeckoLib, Cataclysm, Lionfish API, Curios API) şu klasöre at:

```
.minecraft/mods/
```

### 3. Bixis Mod'u ekle

Bu repodaki `bixis-mod/build/libs/bixis-1.0.0.jar` dosyasını al, aynı şekilde `mods/` klasörüne koy.

### 4. Addon'ları yerleştir

`bixis-addons/` klasöründeki tema klasörlerini (örneğin `bixis_kadikoybogasi`) bul ve şuraya kopyala:

```
.minecraft/addons/lucky/
```

> ⚠️ Klasörün **içeriğini** değil, klasörün **kendisini** kopyala. Yani `addons/lucky/bixis_kadikoybogasi/` şeklinde olmalı.

### 5. Oyunu başlat

Forge 1.20.1 profiliyle Minecraft'ı aç. Yaratıcı modda envanterinde "Bixis Mod" sekmesini bulup ilgili Lucky Block'u alarak test edebilirsin.

---

## 📁 Repo Yapısı

```
bixis-mod/              → Forge Java modu (silahlar, NPC'ler, mob'lar, item'lar)
bixis-addons/            → Yayınlanan Lucky Block temaları
  ├── bixis_kadikoybogasi/
  └── bixis_mehmetsimsek/
```

Yeni bir Lucky Block teması eklemek istersen `!bixis_kadikoybogasi/` gibi bir addon klasörünü kopyalayıp özelleştirmen yeterli!

---

## 🎲 Tema Drop Oranları

Her temanın kendine özgü lucky/unlucky eventleri ve oranları vardır. Detaylı liste için ilgili temanın klasöründeki `DROP_RATES.md` dosyasına bakabilirsin:

- [Kadıköy Boğası — Drop Oranları](bixis-addons/!bixis_kadikoybogasi/DROP_RATES.md)
- [Mehmet Şimşek — Drop Oranları](bixis-addons/bixis_mehmetsimsek/DROP_RATES.md)

---

## 🚧 Yol Haritası

- 🗹 Minigame otomasyonu ()
- [ ] Daha fazla Lucky Block teması/addon'u
- [ ] Boss savaşları (Cataclysm entegrasyonu)
- [ ] Daha fazla event
- [ ] Yapı/structure çeşitliliğinin artırılması

---

## 📺 Bixis

Bu mod, [Bixis YouTube kanalı](https://www.youtube.com/@Bixyis) ve [DragoniteGaming YouTube kanalı](https://www.youtube.com/@DragoniteGaming1606) için geliştirilmektedir. İçerikte kullanılan tüm karakter ve referanslar mizahi/parodik amaçlıdır.