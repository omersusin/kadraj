# Kadraj

**Ekran görüntüleri ve fotoğraflar için editoryal kolaj stüdyosu.**

Kadraj, görsellerin oranını okur; telefon ekran görüntülerini, masaüstü
ekran görüntülerini ve karışık seçkileri ayrı ayrı anlar. Düzeni, boşluğu
ve kadrajı buna göre kurar. Her şey cihazda kalır.

[**Android APK'yı indir**](https://github.com/omersusin/kadraj/releases/latest) ·
Android 10 ve üzeri · Yaklaşık 1 MB

## Neden Kadraj?

Çoğu kolaj uygulaması fotoğrafları eşit karelere sıkıştırır. Kadraj ise önce
seçkideki baskın yönü ve karışık oranları inceler; ardından kompozisyonu
görsellerin kendi diline göre kurar. Telefon ekran görüntüleri uzun kalır,
masaüstü görüntüleri genişler, karışık seçkiler kendi ritmini bulur.

- **Akıllı oran analizi** — portre, manzara ve karışık seçkiler ayrı değerlendirilir.
- **Altı düzen** — Otomatik, Izgara, Akış, Vitrin, Şerit ve Yığın.
- **Yedi tuval oranı** — serbest bırakabilir veya 1:1, 4:5, 9:16, 16:9, 16:10, 3:2 seçebilirsin.
- **Kadraj kontrolü** — görseli sürükleyerek odak noktasını değiştir; çift dokunuşla doldur ve sığdır arasında geçiş yap.
- **Sekiz palet** — Kâğıt, Gece, Kil, Adaçayı, Sis, Gül, Fildişi ve Zeytin.
- **İnce ayarlar** — aralık, kenar, köşe, paspartu, gölge ve film dokusu.
- **Cihazda üretim** — JPEG veya PNG, 2K ve 4K çıktı; galeride `Kadraj` albümü.
- **Hesap yok, bulut yok** — internet izni yok; görseller cihazdan ayrılmaz.

## Düzenler

| Düzen | Ne zaman? |
| --- | --- |
| **Otomatik** | Seçkinin yönünü ve çeşitliliğini okur; en dengeli düzeni seçer. |
| **Izgara** | Eşit ritim isteyen, aynı orandaki fotoğraflar için. |
| **Akış** | Farklı yükseklikleri koruyan serbest kolon kompozisyonu. |
| **Vitrin** | Tek bir kahraman görsel ve onu tamamlayan küçük kareler. |
| **Şerit** | Yatay ya da dikey, sinematik bir dizilim. |
| **Yığın** | Hafif dönüşlerle elde yerleştirilmiş gibi duran katmanlar. |

## Kullanım

1. Galerinden 1–12 görsel seç.
2. Şablonu ve tuval oranını belirle; Kadraj önizlemeyi anında kurar.
3. Görsele dokunarak seç, sürükleyerek kadrajı kaydır.
4. Stil sekmesinden paleti, boşlukları ve dokuyu ayarla.
5. Boyut ve formatı seçip galeriye kaydet; istersen doğrudan paylaş.

## Kurulum

1. [Son sürüm APK'yı indir](https://github.com/omersusin/kadraj/releases/latest).
2. Android ayarlarından bu kaynağa uygulama yükleme izni ver.
3. APK'yı aç ve kur.

> APK `com.omersusin.kadraj` paketiyle imzalıdır. Sonraki sürümler aynı imza
> anahtarıyla yayınlanır; güncellemeler veri kaybı olmadan yüklenebilir.

## Geliştirme

Gereksinimler: JDK 17, Android SDK 35 ve Gradle 8.9.

```bash
git clone https://github.com/omersusin/kadraj.git
cd kadraj
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

İmzalı sürüm derlemesi için şu ortam değişkenleri gerekir:

```bash
KADRAJ_KEYSTORE=/path/to/release.p12
KADRAJ_STORE_PASSWORD=...
KADRAJ_KEY_ALIAS=kadraj
KADRAJ_KEY_PASSWORD=...
./gradlew assembleRelease
```

Çıktı: `app/build/outputs/apk/release/app-release.apk`.

GitHub Actions her `main` gönderiminde testleri ve R8'li sürüm derlemesini
çalıştırır; imzalı APK'yı `zipalign` ve `apksigner` ile doğrular. `v*`
biçimindeki etiketler APK'yı GitHub Releases'e yayınlar.

## Teknik yapı

- Kotlin 2.0 ve Jetpack Compose; Material kütüphanesi kullanılmaz.
- Saf Kotlin düzen motoru ve JVM birim testleri.
- Önizleme ile dışa aktarma aynı Canvas ressamını paylaşır.
- R8 küçültme, kaynak küçültme ve imzalı release yapılandırması.
- `minSdk 29`, `targetSdk 35`.

## Yazı tipleri

Başlıklar **Fraunces**, arayüz **Manrope** ile dizilir. Her iki yazı tipi de
SIL Open Font License 1.1 kapsamında kullanılır; lisanslar
[`licenses/`](licenses/) altındadır.

## Lisans

[MIT](LICENSE) · Ömer SÜSİN
