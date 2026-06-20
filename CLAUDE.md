# Bixis Mod & Addon — Claude Code Context

## Proje Özeti
Minecraft 1.20.1 Forge modu ve Lucky Block addon paketi.
YouTube kanalı Bixis için üretilen karışık lucky block içeriği.

> **Not — v1.0.1 Minigame Sistemi:** `MINIGAME_DESIGN.md` dosyası MUTLAKA okunmalı; tüm state machine, komut referansı ve veri mimarisi orada tanımlı.

## Teknik Stack
- Minecraft: 1.20.1
- Mod Loader: Forge 47.4.10
- Dil: Java 17
- Bağımlılıklar: Lucky Block Mod(13.0), L_Ender's Cataclysm(3.31), GeckoLib(4.8.3), Lionfish API (3.0, Cataclysm dependency'si, build.gradle'a ekleme),
Curios API (5.14.1, Cataclysm dependency'si, build.gradle'a ekleme)
- Build: Gradle

## Repo Yapısı
bixis-mod/         → Forge Java modu
bixis-addon/       → Lucky Block JSON addon paketi

## bixis-mod İçeriği
- bixis:turk_lirasi         → Para birimi item
- bixis:fenerbahce_kilici   → Hasar 10, diamond sword hızı
- bixis:fenerbahce_forma   → Custom chestplate (zırh 9, tokluğu 4, KBR 0.1), giyince Strength I
- bixis:tc_pasaportu        → Ölümde Totem of Undying etkisi (aşağıya bkz.)
- bixis:faiz_kalkani        → Normal item (stack 1, EPIC=pembe isim), elde tutulunca Absorption biriktirir; her 400 tick'te (20 sn) +1 sayaç (max 5), Absorption amplifier=sayaç-1; hasar alınca sayaç mevcut absorption miktarına senkronize edilir ve tick sayacı sıfırlanır; item elden çıkınca Absorption silinir, sayaç sıfırlanır; FaizKalkaniEventHandler (PlayerTickEvent + LivingDamageEvent); lore: satır 1 beyaz açıklama, satır 2 italik gri "Merkez Bankası faizi koruyor."
- Silah sistemi             → 4 tip x 3 malzeme = 12 silah + ateş_asası (aşağıya bkz.)
- Desert Eagle (tabanca)    → GunItem tabanlı, 7 şarjör, reload sistemi (aşağıya bkz.)
- Kanama efekti             → bixis:kanama — öldürücü bleeding efekti
- Shop NPC                  → TL karşılığı item satışı
- Döviz Bürosu NPC          → Materyal → TL dönüşümü
- Admin komutları           → /bixis give, /bixis setrate vb.; /bixis vergizamani: tüm oyunculardan 5-25 random TL sil, kırmızı bold title, gri italik chat + per-oyuncu log, wither spawn sesi (op level 2)
- Boss'lar                  → Cataclysm API üzerinde Bixis boss'ları ve custom boss'lar
- bixis:hirt               → Saldırgan mob, yerden item alır (%100), %10 iron hancer ile doğar
- bixis:recep_ivedi        → Nötr mob, 40-80 tickte bir 3 blok yarıçapında rastgele blok kırar
- bixis:kemal_darkilicoglu → Tamamen pasif, sandalyede oturur; drop: 1x mutlak_butlan; spawn'da stairs + ArmorStand (Y = stairsPos.getY()-0.5, NoGravity=true), anında biner, Kemal'e de setNoGravity(true); koltuk oyuncuya dönük (Direction.fromYRot().getOpposite()); ChairUUID NBT; ölünce ArmorStand discard
- bixis:holigan_fenerbahce / bixis:holigan_galatasaray / bixis:holigan_besiktas / bixis:holigan_trabzonspor → Holigan mob sistemi (aşağıya bkz.)
- bixis:george_floid       → Pasif mob, spawn'da infinite Kanama efekti, 60 tick sonra chat mesajı, hurt sesi: george_floid_hurt, drop: 1x fent
- bixis:turk_polisi        → Hostile mob (50 HP); `PolisState` enum: HOSTILE/RUSVET/MOB_FIGHT — state değişince `applyState()` ekipmanı günceller; `state` field başlangıçta null — ilk `applyState(HOSTILE)` çağrısı geçer; `needsInitialEquip` flag: ilk tick'te `state=null` yapıp `applyState()` çağırır — spawn-time sync sorunu (entity tracked olmadan set edilen ekipman client'a ulaşmayabiliyor) yerine runtime'da güvenilir sync sağlar; NBT'ye kaydedilmez; renderer'a `ItemInHandLayer` eklendi (olmadan mob elindeki item render edilmez — HumanoidMobRenderer bu katmanı otomatik eklemez); HOSTILE: netherite_sword (Sharpness II, Knockback II) + shield; RUSVET: MainHand boş + OffHand TL; MOB_FIGHT: netherite_sword + OffHand TL; hedef önceliği: hostile moblar (priority 3) > oyuncu (priority 5) — Zombie/Skeleton/Spider/CaveSpider/Creeper/Witch/Pillager/Vindicator/Evoker/WitherSkeleton/Drowned/Husk/Stray + 4 holigan + Hirt; TurkPolisi/Abugat/RecepIvedi hedef değil; rüşvet: 4 blok içine TL düşünce 3000 tick RUSVET, passive moddayken yeni TL süreyi sıfırlar, oyuncu hedefi engellenir ama hostile mob hedefi devam eder; tüm sesler null; drop şansı hepsi 0.0f
- bixis:abugat             → Pasif mob, dans mekaniği (döner + zıplar), idle/sing sesleri (çakışma korumalı), drop: desert_eagle+mermi+TL
- bixis:puro               → Consume item (EAT anim, 32 tick), duman partikülü (SMOKE 5 adet) + FIRE_EXTINGUISH sesi (0.5f, 1.5f pitch), efekt yok; use() override ile startUsingItem
- bixis:fent               → Consume item (DRINK anim, 32 tick), Poison II + Nausea II (400t); use() override ile startUsingItem
- bixis:mutlak_butlan      → Tüketilmez (stack 1, EPIC), main/off hand'de tutulunca Resistance II (infinite); MutlakButlanEventHandler PlayerTickEvent ile yönetir; Kemal Darkılıçoğlu dropu

## bixis-addon İçeriği
- Tema: Kadıköy Boğası / Mehmet Şimşek / Takla King / ...
- Her tema: lucky.json + unlucky.json
- universal/ → filler eventler (tüm bloklarda ortak)
- Loot table'larda bixis-mod item ve entity'leri referans alınır
- Döviz: bazı lucky eventlerde bixis:turk_lirasi düşer

## bixis-addon Yapısı
**Lucky Block Mod 13.0 addon formatı `.txt` DSL kullanır — JSON değil.**

Doğru addon yapısı (`bixis-addons/{addon_id}/`):
```
{addon_id}/
  plugin_init.txt          → block_id={addon_id}
  properties.txt           → spawnrate, structureChance
  drops.txt                → tüm drop eventleri (lucky + unlucky tek dosyada)
  pack.mcmeta              → pack_format: 18 (1.20.1)
  assets/lucky/
    textures/block/
      {addon_id}.png       → 16x16 blok texture
    blockstates/
      {addon_id}.json      → variants: model referansı
    models/block/
      {addon_id}.json      → parent: block/cube_all, textures.all
    models/item/
      {addon_id}.json      → parent: lucky:block/{addon_id}
    lang/
      en_us.json           → block.lucky.{addon_id}: "İsim"
```

### drops.txt DSL Formatı
```
/Yorum satırı
type=item,ID=mod:item_id,amount=#rand(min,max)@luck=N@chance=0.N
type=entity,ID=mod:entity_id,NBTTag=(CustomName=#jsonStr(text="İsim",color=blue,bold=true))@luck=N@chance=0.N
group(type=item,ID=...;type=entity,ID=...)@luck=N@chance=0.N
```
- `luck`: pozitif = şanslı, negatif = şanssız; mutlak değeri arttıkça nadirleşir
- `chance`: 0.0-1.0, bu eventin seçim havuzuna girme olasılığı
- `#rand(a,b)`: a-b arası rastgele sayı
- `#jsonStr(text="...",color=X,bold=true)`: entity isim komponenti
- `#randFireworksRocket`: rastgele havai fişek NBT
- `#circleOffset(r)` / `#circleOffset(minR,maxR)`: daire içi konum
- `#luckySwordEnchantments`, `#luckyToolEnchantments`, `#luckyBowEnchantments` vb.: mod dahili şans büyüsü setleri
- `#chestLootTable("chests/...")`: vanilla loot table ile sandık doldurma
- `type=structure` çalışmıyor. Yapı generate için `type=command,ID="/place structure minecraft:igloo ~ ~ ~"` kullan.
- Addon içindeki custom yapılar (`structures/*.nbt`) `lucky:` namespace'i ile erişilir: `type=command,ID="/place structure lucky:enchanting_setup ~ ~ ~"`
- Custom `type=structure` için `structures.txt` zorunlu: `ID=enchanting_setup,file=enchanting_setup.nbt,centerY=1` — bu dosya olmadan event sessizce başarısız olur. Çalışıyor ✓ (rotasyon .nbt kayıt yönüne göre ayarlanmalı)
- `#randList(a,b,c)`: listeden rastgele seçim

**ÖNEMLI — inherit YOK:** Custom `block_id` tamamen boş bir pool oluşturur.
Orijinal lucky block eventleri otomatik gelmez; drops.txt'e manuel yazılması gerekir.
**Encoding:** Tüm .txt dosyaları UTF-8 BOM'suz (UTF-8 without BOM) kaydedilmeli.

### Mevcut Addon'lar (bixis-addons/)
| Klasör               | block_id              | Texture | Özel Eventler |
|----------------------|-----------------------|---------|---------------|
| bixis_kadikoybogasi/ | bixis_kadikoybogasi   | #003366 lacivert | DROP_RATES.md mevcut ✓ | Overlay: multipart blockstate → bixis_kadikoybogasi_overlay model (cutout, lucky:block/lucky_block_overlay, frametime 10 frames [0,1,1,0]); Lucky: Aziz Yıldırım Iron Golem (luck +4, %30), Fenerbahçe Holigan sürüsü 3-5 adet (luck +3, %35), Giant Zombie (luck 0, %40); tüm iron/diamond/netherite yakın dövüş silahları (hancer/mizrak/yatagan/gaddare), netherite → havai fişek; structure: /place komutu ile igloo (%25) ve type=structure ile enchanting_setup (%40, luck +3, structures.txt ile kayıtlı, çalışıyor ✓) ve jailtrap (unlucky, luck -1, %50, structures.txt ile kayıtlı); lucky_altar_1-4 (luck +3/+2/+1/0, %40); unlucky_altar_1-4 (luck -3/-2/-1/0, %40); Unlucky: Ali Koç elmas zombi (luck -7, %30, zırh/silah %20 drop şansı), George Floid, Recep İvedi, Kemal Darkılıçoğlu, Hirt+George kombo, Galatasaray/Beşiktaş/Trabzonspor Holigan sürüsü 3-5 adet (luck -5, %35), Dev Slime Size=5 (luck -4, %30), Dev Magma Küpü Size=5 (luck -4, %30), İcardi altın zırhlı zombie (luck -5, %30), Osimhen demir+iron_gaddare zombie (luck -5, %30), Muslera netherite+netherite_gaddare zombie (luck -6, %25); silah dropları: sadece yakın dövüş (ateş_asası %10 şans), ateşli silah yok; efekt limitleri: pozitif max 300t, negatif max 100t |

| bixis_mehmetsimsek/ | bixis_mehmetsimsek    | #2E7D32 yeşil — DROP_RATES.md mevcut ✓ | Lucky: Faiz Kalkanı (bixis:faiz_kalkani, luck +1, %40), Yeşil Pasaport + havai fişek (tc_pasaportu+fireworks, luck +2, %30), TOKİ yapısı (structure, luck +3, %30, structures.txt ile kayıtlı); Unlucky: Türk Polisi spawn 1-4 adet (luck -2, %50), Vergi Zamanı (/bixis vergizamani komutu, luck -4, %50) |

TODO: Takla King vb. addon klasörleri henüz oluşturulmadı.

## Döviz Sistemi
- 1 altın = 1 TL
- 1 elmas = 2 TL
- Oranlar config/bixis-rates.json'dan okunur

## Kod Kuralları
- Her feature ayrı package içinde olsun
- Magic number yok, sabitler Constants.java'da
- Her public metoda Javadoc

## Fenerbahçe Forması Mekaniği
Registry: `bixis:fenerbahce_forma` | ArmorItem + BixisArmorMaterials.FENERBAHCE_FORMA
Armor material: `item/BixisArmorMaterials.java` (enum implements ArmorMaterial)
- Zırh: 9 (netherite chestplate 8'di, 1 tık üstü)
- Tokluğu: 4 | KBR: 0.1 | Enchant: 15 | Ses: leather equip
- Giyince Strength I (amplifier 0, Integer.MAX_VALUE tick, gösterge gizli)
- Çıkarınca Strength efekti kaldırılır
Handler: `item/FenerbahceFormaEventHandler.java` — `LivingEquipmentChangeEvent` dinler, CHEST slot değişince efekti ekler/kaldırır
Armor texture: `assets/bixis/textures/models/armor/fenerbahce_forma_layer_1.png` (ArmorMaterial name: "bixis:fenerbahce_forma")
Item texture: `assets/bixis/textures/item/fenerbahce_forma.png` (placeholder sarı 16x16)
Config key: `fenerbahce_forma_price` (Constants: DEFAULT_FENERBAHCE_FORMA_PRICE = 15)
Lang: tr_tr "Kadıköy Boğası'nın Forması", en_us "Bull of Kadikoy Jersey"

## Mob Sistemi

### Holigan Sistemi
Base: `mob/HoliganEntity.java` extends `Monster` | Alt sınıflar: `FenerbahceHoliganEntity`, `GalatasarayHoliganEntity`, `BesiktasHoliganEntity`, `TrabzonsporHoliganEntity`
- `Team` enum: FENERBAHCE, GALATASARAY, BESIKTAS, TRABZONSPOR — her alt sınıf `getHoliganTeam()` override eder (NOT: `getTeam()` değil — Minecraft Entity base sınıfıyla çakışır)
- Oyuncu vurursa: 32 blok içindeki aynı takım holiganları da o oyuncuyu hedef alır (`hurt()` override)
- Her 40 tick'te: 16 blok içinde farklı takım holigan varsa onu hedef alır, oyuncuyu unutur (`aiStep()`)
- Rakip menzil dışına çıkınca target temizlenir
- Hurt/death/ambient ses yok (null döner)
- Saldırıda: `holigan_aggro` sesi
- Ambient ses: `getAmbientSound()` abstract override — her alt sınıf kendi sesini döndürür; `getAmbientSoundInterval()` = 1200 tick (60 sn); Trabzonspor null döndürür (ses yok); Minecraft'ın kendi ambient sistemi entity başına yönetir (global cooldown yok)
- Trabzonspor: spawn sesi yok (`getSpawnSound()` null)
- Renderer: `client/HoliganRenderer.java` — constructor'da ResourceLocation alır, 4 tip paylaşır
- Spawn egg renkleri: FB lacivert+sarı, GS kırmızı+sarı, BJK siyah+beyaz, TS bordo+mavi
- Spawn egg model JSON: `assets/bixis/models/item/holigan_*_spawn_egg.json` — parent: `item/template_spawn_egg`
- Ses kayıtları: `holigan_aggro`, `holigan_fenerbahce_spawn`, `holigan_galatasaray_spawn`, `holigan_besiktas_spawn`
- JVM JIT crash fix (build.gradle): C2 compiler belirli metodları native'e çevirirken EXCEPTION_ACCESS_VIOLATION ile çöküyor; etkilenen metodlar `jvmArg -XX:CompileCommand=exclude,...` ile devre dışı bırakılır:
  - `net/minecraft/client/model/HumanoidModel,setupAnim` — HumanoidModel renderer crash'i
  - `net/minecraft/world/level/entity/EntitySection,getEntities` — entity tick crash'i (hs_err_pid34152)

### Hırt
Registry: `bixis:hirt` | Class: `mob/HirtEntity.java` | extends `Monster`
- Saldırgan, oyuncuyu görünce attack (NearestAttackableTargetGoal)
- `canPickUpLoot()` = true — yerden %100 item alır (vanilla mekanik)
- `getEquipmentDropChance(MAINHAND)` = 2.0f → %100 drop; diğer slotlar vanilla varsayılanı
- `populateDefaultEquipmentSlots`: %25 şansla iron_hancer ile doğar; silahsız spawn değişmez
- Sesler: `hirt_idle` (sounds.json'da hirt_idle_1+2 çoklu), `hirt_aggro` (hirt_aggro_1+2 çoklu)
- Hurt/death: vanilla vindicator sesleri
- Idle aralığı: 80-160 tick
- Texture sistemi: 4 varyant — `EntityDataAccessor<Integer> DATA_VARIANT` ile synced
  - Spawn'da `finalizeSpawn()` içinde `random.nextInt(4)` ile seçilir
  - NBT key: `HirtVariant` (int 0-3) — chunk reload'da korunur
  - Renderer: `client/HirtRenderer.java` extends HumanoidMobRenderer
  - hirt_1.png (gri), hirt_2.png (mor), hirt_3.png (kırmızı), hirt_4.png (yeşil)
- Spawn egg: `bixis:hirt_spawn_egg` (koyu gri + kırmızı)

### Recep İvedi
Registry: `bixis:recep_ivedi` | Class: `mob/RecepIvediEntity.java` | extends `PathfinderMob`
- Nötr (oyuncuya saldırmaz), yavaş yürüyen mob
- `aiStep()` blok kırma: her 40-80 tickte bir 3 blok yarıçapında rastgele blok kırar (`destroyBlock` → item drop)
- `aiStep()` yumruk: her 160 tickte bir (8 sn) 8 blok yarıçapındaki rastgele bir entity'e 3 hasar verir
  - Kendine vuramaz; oyunculara da vurabilir; vuruşta recep_idle sesi (pitch 1.2f)
- Kırılmayan bloklar: bedrock, obsidian, chest, spawner, command block, barrier, end portal frame vb.
- Ses: `recep_idle` (recep_ivedi_1.ogg), 60-120 tick | Hurt/death: ses yok
- Texture: `assets/bixis/textures/entity/recep_ivedi.png`
- Spawn egg: `bixis:recep_ivedi_spawn_egg` (toprak kahvesi + sarı)

## TC Pasaportu Mekaniği
Handler: `item/TcPasaportuEventHandler.java` — `LivingDeathEvent` dinler.
Tetiklenme koşulu: offhand veya mainhand'de `bixis:tc_pasaportu` varsa:
1. Ölüm iptal edilir (event.setCanceled(true))
2. Oyuncu canı 1'e ayarlanır
3. Tüm efektler temizlenir
4. Speed II + Jump Boost II + Regeneration I — 300 tick (15 sn) uygulanır
5. Chat'e yayın: "[oyuncu adı] pasaportunu kullandı. İyi yolculuklar!"
6. Pasaport 1 adet tüketilir (shrink)
Sabitler: Constants.java'da `TC_PASAPORTU_EFFECT_TICKS`, `TC_PASAPORTU_SPEED_AMP`, vb.

## NPC Sistemleri

### Rahim Koç (Döviz Bürosu)
Registry: `bixis:rahim_koc` | Java class: `RahimKocEntity`
Model: Steve skeleton, texture: assets/bixis/textures/entity/rahim_koc.png
Conversion trades (VER → AL):
- 1 iron_ingot → 1 bixis:turk_lirasi
- 1 gold_ingot → 1 bixis:turk_lirasi
- 1 emerald → 1 bixis:turk_lirasi
- 1 diamond → 2 bixis:turk_lirasi
- 1 netherite_ingot → 2 bixis:turk_lirasi
- 1 eye_of_ender → 2 bixis:turk_lirasi
- 1 beacon → 5 bixis:turk_lirasi
- 1 nether_star → 5 bixis:turk_lirasi

### Villa Hakan (Shop)
Registry: `bixis:villa_hakan` | Java class: `VillaHakanEntity`
Model: Steve skeleton, texture: assets/bixis/textures/entity/villa_hakan.png
Shop trades (VER → AL):
- 15 TL → bixis:fenerbahce_forma (custom chestplate, zırh 9, giyince Strength I)
- 10 TL → bixis:m4 (FullAutoGunItem, 30 şarjör, hasar 6, full-auto)
- 10 TL → bixis:awp (GunItem, 5 şarjör, hasar 20, yarı oto, 1sn cooldown)
- 7 TL → bixis:desert_eagle (GunItem, 7 şarjör, hasar 8, yarı oto)
- 1 TL → bixis:mermi x32
- 15 TL → bixis:fenerbahce_kilici (placeholder texture, diamond sword statları)
- 3 TL → bixis:tc_pasaportu
- 1 TL → lucky:lucky_potion x3, display: "ProteinOşın" (Lucky Block modu yüklü değilse eklenmez)

## Minigame Sistemi (v1.0.1)

### State Machine İskeleti
Package: `minigame/`
- `GameState.java` — enum: `LOBI, GERI_SAYIM, YARIS, HAZIRLIK_1, HAZIRLIK_2, KAPISMA, SONUC`
- `GameStateManager.java` — singleton (`INSTANCE`), `getState()` / `setState(GameState)`, değişimde konsola log (`[Bixis] X -> Y`)
- `/bixis durum` — mevcut GameState'i chat'e yazar (herkes)
- `/bixis sifirla` — GameState'i LOBI'ye resetler (op 2) + LobbyManager.reset() çağırır
- `/bixis basla` — GameState LOBI + tüm dolu takımlar hazır olmalı; geçer → GERI_SAYIM + `LobbyManager.startCountdown()`

### Lobi Fazı
`LobbyManager.java` — `@Mod.EventBusSubscriber` singleton, MinecraftForge event bus'a otomatik kayıtlı
- **Scoreboard takımları:** `bixis_team1`–`bixis_team4`, renkler: RED/GREEN/YELLOW/BLUE; sunucu başlangıcı ve `/bixis sifirla`'da yeniden oluşturulur, üyeler temizlenir
- **Ready flags:** `boolean[4]` — takım üyesi eklenince/çıkınca ilgili takım flag'i false'a döner
- `/bixis takimsec <1-4>` — GameState LOBI değilse hata; oyuncu zaten o takımdaysa "Zaten X. takımdasın!" hata sesi; aksi halde eski takımdan çıkarır, yeni takıma ekler, her iki takımın flag'ini sıfırlar
- `/bixis hazir <1-4>` — oyuncu o takımda değilse hata; `readyFlags[i] = true`; tüm sunucuya takım renginde broadcast; tüm dolu takımlar hazırsa konsola log
- **Sidebar scoreboard:** `bixis_lobby` objective, her 20 tick'te güncellenir; sadece LOBI fazında görünür, diğer fazlarda kaldırılır; satır formatı: `"§X Takım N: M oyuncu - DURUM"` (DURUM: `✔` hazır, `✘` bekliyor, `-` üye yok)
- **Sesler:** başarı → `SoundEvents.UI_BUTTON_CLICK` (playNotifySound); hata → `SoundEvents.VILLAGER_NO`; LOBI dışı komut hatası da hata sesi çalar
- **Hata mesajı formatı:** tüm hata/uyarı mesajları `ChatFormatting.DARK_RED` + `⚠` öneki; başarı mesajları takım rengiyle kalsın
- **Geri Sayım fazı:** `LobbyManager.startCountdown()` → `ServerTickEvent` ile tick 0/20/40'ta title "3/2/1", tick 60'ta ışınlama+kit+ses+title "YARIŞ BAŞLADI!" ve YARIS state'ine geçiş; spawn ayarlanmamış takımlar ışınlanmaz, konsola warn; 3/2/1'de `UI_BUTTON_CLICK` tik sesi çalar
- **Kit içeriği:** iron_pickaxe x1, cooked_beef x10, cobblestone x32
- **Race Spawn Config:** `config/bixis-race-spawns.json` — `BixisRaceSpawnsConfig`; `setSpawn(teamNum, x, y, z, yaw, dimension)` anında dosyaya yazar; `/bixis admin set race <1-4>` ile kaydedilir

### Admin Harita Kurulum Komutları
Tüm komutlar `/bixis admin ...` altında, op level 2 gerektirir.
**Config sınıfları** (package: `config/`):
- `SpawnPoint.java` — paylaşılan record `(double x, y, z, float yaw, String dimension)` — tüm config'lerde kullanılır
- `BixisRaceSpawnsConfig` — `config/bixis-race-spawns.json` (4 takım başlangıç noktası)
- `BixisArenaSpawnsConfig` — `config/bixis-arena-spawns.json` (4 takım arena spawn)
- `BixisCheckpointsConfig` — `config/bixis-checkpoints.json` (takım başına checkpoint listesi, append semantics)
- Her üçü de: `init(configDir)`, `clearAll()`, `loadFrom(Path)` metodlarına sahip
- `BixisArenaSpawnsConfig.parseSpawnPoint()` / `.spawnPointToJson()` — paylaşılan JSON helper (diğer config'ler de kullanır)

**Komut ağacı:**
```
/bixis admin set race <1-4>        → oyuncu konumunu race spawn kaydeder
/bixis admin set arena <1-4>       → oyuncu konumunu arena spawn kaydeder
/bixis admin set checkpoint <1-4>  → oyuncu konumunu takımın CP listesine ekler (sıra no döner)

/bixis admin list race             → tüm 4 takımın race spawn'larını yazar
/bixis admin list arena            → tüm 4 takımın arena spawn'larını yazar
/bixis admin list checkpoint <1-4> → o takımın CP listesini #1, #2, ... ile yazar

/bixis admin remove checkpoint <1-4> <sira_no>  → 1-based sıra numarasıyla CP siler, listesi kaydırılır

/bixis admin reset all             → tüm 3 config dosyasını temizler

/bixis admin tp race <1-4>                    → admin'i race spawn'a ışınlar
/bixis admin tp arena <1-4>                   → admin'i arena spawn'a ışınlar
/bixis admin tp checkpoint <1-4> <sira_no>    → admin'i o CP'ye ışınlar

/bixis admin loadmap <isim>        → config/maps/<isim>/{race-spawns,arena-spawns,checkpoints}.json'u aktif config'lerin üzerine kopyalar + yeniden yükler
```
**loadmap dizin yapısı:** `config/maps/<harita_ismi>/race-spawns.json`, `arena-spawns.json`, `checkpoints.json` — en az biri bulunmalı; eksik dosyalar atlanır.

## Silah Sistemi

### Kanama Efekti
Registry: `bixis:kanama` | Class: `effect/KanamaEffect.java`
- Her 8 tick'te bir magic hasar, Amplifier II → her 4 tick
- Zehirden farklı: %1 cana kadar değil, öldürür
- Texture: `textures/mob_effect/kanama.png` (18x18 kırmızı)

### Silah Stat Tablosu
| İsim             | Hasar | Hız   | Özellik |
|------------------|-------|-------|---------|
| iron_hancer      | 4     | çok hızlı (0.0f) | — |
| diamond_hancer   | 5     | çok hızlı | — |
| netherite_hancer | 6     | çok hızlı | — |
| iron_mizrak      | 6     | -2.4f | +2 reach, sağ tık fırlat, saplar+kalır, sağ tık/geç → geri al |
| diamond_mizrak   | 6     | -2.4f | aynı |
| netherite_mizrak | 7     | -2.4f | aynı |
| iron_yatagan     | 6     | -2.4f | %8 Kanama II (140t) |
| diamond_yatagan  | 7     | -2.4f | %8 Kanama II (140t) |
| netherite_yatagan| 8     | -2.4f | %8 Kanama II (140t) |
| iron_gaddare     | 9     | -3.0f | %15 Slowness II (2sn), <%30 canda x2 hasar |
| diamond_gaddare  | 9     | -3.0f | aynı |
| netherite_gaddare| 9     | -3.0f | aynı |
| ates_asasi       | 5     | -2.4f | sağ tık: 6 blok AoE ateş, 8sn cooldown |

### Sınıf Yapısı
- `weapon/YataganItem.java` — Kanama, `hurtEnemy()` override
- `weapon/GaddareItem.java` — Slowness, `hurtEnemy()` override
- `weapon/MizrakItem.java` — Reach attribute + `use()` throw
- `weapon/MizrakProjectileEntity.java` — AbstractArrow subclass, variant NBT ile pickup
- `weapon/AtesAsasiItem.java` — Item subclass, attribute modifier + `use()` AoE fire
- `weapon/WeaponEventHandler.java` — Gaddare x2 damage (LivingHurtEvent), Ateş Asası actionbar

## Desert Eagle / Tabanca Sistemi

### GunItem (weapon/GunItem.java)
Temel yarı-otomatik silah sınıfı. Constructor:
`GunItem(magazineSize, reloadTicks, throwSpeed, bulletDamage, shotCooldownTicks, gunName, fireSound, emptySound, equipSound, reloadSound, Properties)`
NBT: `BixisAmmo` (int) — şarjördeki mevcut mermi sayısı (yoksa şarjör dolu kabul edilir)

### FullAutoGunItem (weapon/FullAutoGunItem.java)
GunItem extend eder, tam otomatik ateş. Constructor ekler: `fireRateTicks`.
`getUseDuration()=72000`, `onUseTick()` her `fireRateTicks` tick'te `fireOnce()` çağırır.
İlk ateş `use()` içinde anında gerçekleşir.

### Silah Değerleri
| Silah         | Registry              | Şarjör | Reload | Hasar | Mod        | Cooldown |
|---------------|-----------------------|--------|--------|-------|------------|----------|
| Desert Eagle  | bixis:desert_eagle    | 7      | 30t    | 8     | Yarı oto   | 5t       |
| AWP           | bixis:awp             | 5      | 60t    | 20    | Yarı oto   | 20t      |
| M4            | bixis:m4              | 30     | 50t    | 6     | Tam oto    | 2t/ateş  |

### Mermi Akışı
1. Sağ tık → şarjörde mermi varsa ateş, BixisAmmo--
2. Şarjör bitti → envanterde yeterli bixis:mermi varsa auto-reload başlar
3. Mermi yoksa: actionbar "Mermi yok!" + boş tetik sesi
4. Reload süresince: actionbar "Şarjör dolduruluyor... (Xsn)"
5. Reload bitti: envanterden mermiler düş, BixisAmmo dolar, actionbar "[SilahAdı] hazır! [■■■] N/N"
6. Oyuncu silahı bırakırsa reload iptal olur

### Manuel Reload (Sol Tık)
GunEventHandler: `AttackEntityEvent`, `LeftClickBlock`, `LeftClickEmpty` yakalanır.
Şarjör dolmamışsa reload başlatılır, saldırı event'i iptal edilir.
Reload sırasında sol tık saldırısı da engellenir.

### GunEventHandler (weapon/GunEventHandler.java)
Singleton: `GunEventHandler.INSTANCE`, Forge event bus'a kayıtlı.
Statik map'ler: `reloadTicksLeft`, `readyDisplayTicks`, `wasHoldingGun` (UUID → değer)
Public API: `GunEventHandler.isReloading(UUID)`, `GunEventHandler.startReload(UUID, ticks)`
Logout'ta tüm state temizlenir (PlayerLoggedOutEvent).

### BulletProjectileEntity (weapon/BulletProjectileEntity.java)
ThrowableItemProjectile extend eder. Gravity: 0.02f. Çarpışmada discard.
Render: ThrownItemRenderer → bixis:mermi item modeli.

### Sesler (weapon/BixisSounds.java)
Registry: `DeferredRegister<SoundEvent>`, `BixisSounds.register(modBus)` ile kayıtlı.
- Desert Eagle: `desert_eagle_fire`, `desert_eagle_empty`, `desert_eagle_equip`, `desert_eagle_reload`
- AWP: `awp_fire`, `awp_empty`, `awp_equip`, `awp_reload`
- M4: `m4_fire`, `m4_empty`, `m4_equip`, `m4_reload`
.ogg dosyaları: `assets/bixis/sounds/` | JSON: `assets/bixis/sounds.json`

## Önemli Notlar
- bixis-addon, bixis-mod kurulu olmadan çalışmaz
- Boss'lar Cataclysm API'sine bağımlı, Cataclysm olmadan spawn olmaz
- Tüm fiyatlar config/bixis-rates.json'dan okunur, hardcode etme
- config/bixis-rates.json'da JSON anahtarları: "rahim_koc" ve "villa_hakan"
- Geriye uyumluluk: BixisRatesConfig eski "rahmi_koc"/"killa_hakan" anahtarlarını da okur
