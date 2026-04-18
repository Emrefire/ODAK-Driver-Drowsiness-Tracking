# Yapay Zeka Destekli Yorgunluk Tespit ve Uyarı Sistemi (AI-Based Fatigue Detection and Warning System)

Selamlar! Bu repo, sürücülerin direksiyon başında uyuklamasını veya dikkatinin dağılmasını engellemek amacıyla geliştirdiğim gerçek zamanlı bir tespit sistemini içeriyor.

Dünya genelinde trafik kazalarının büyük bir kısmı yorgunluk ve dikkat dağınıklığından kaynaklanıyor. Bu projede, bilgisayarlı görme (Computer Vision) tekniklerini kullanarak bu soruna pratik bir çözüm getirmeyi amaçladık. 
Kamera üzerinden alınan görüntüleri işleyerek sürücünün göz ve ağız hareketlerini analiz ediyor, eğer bir "tehlike" sezerse sesli uyarı veriyor.

## Projenin Amacı Nedir?
Temel hedef basit: Can güvenliği. Sürücünün gözlerinin kapanma süresini, esneme sıklığını ve baş hareketlerini takip ederek, uyku haline geçmeden önce onu uyarmayı amaçlıyoruz. 
Bunu yaparken de pahalı donanımlara ihtiyaç duymadan, sadece bir web kamerası, güçlü bir nesne tespit modeli (YOLO) ve matematiksel algoritmalarla çözüm üretmek istedik.

## Kullanılan Teknolojiler ve Kütüphaneler
Projeyi geliştirirken şu teknolojilerden faydalandım:

* **Python:** Geliştirme için kullandığımız yazılım dili.
* **YOLO (You Only Look Once):** Nesne tespiti konusunda en hızlı ve kararlı modellerden biri olduğu için sürücünün yüzünü ve kafa yapısını tespit etmekte YOLO'yu kullandım.
* **PyTorch:** YOLO modelinin arka planda koşmasını sağlayan derin öğrenme kütüphanesi.
* **OpenCV:** Görüntü işleme ve kamera akışı için kullandık.
* **MediaPipe:** Yüzdeki kilit noktaları (landmark) tespit etmek için kullandığımız Google'ın kütüphanesi. Özellikle 468 yüz noktasını çok hızlı yakalıyor.
* **NumPy:** Matematiksel hesaplamalar ve vektör işlemleri için.
* **Pygame / Playsound:** Uyarı sesi çalmak için.

## Nasıl Çalışır? (Mantığı Ne?)
Sistem, sadece basit bir görüntü işleme projesi değil; derin öğrenme destekli hibrit bir mantıkla çalışıyor. Süreç döngüsü şu şekilde:

1. **Görüntü Yakalama:** Kameradan anlık kareler (frame) alınır.

2. **YOLO ile Tespit:** Alınan görüntü önce YOLO modeline beslenir. Model, sahne ne kadar karmaşık olursa olsun sürücünün yüzünü ve ilgili bölgeleri yüksek doğrulukla bulur ve koordinatlarını çıkarır.

3. **Geometrik Analiz ve İşleme:** YOLO'nun tespit ettiği bu bölgeler üzerinde, göz kapakları ve dudak mesafeleri için EAR (Eye Aspect Ratio) ve MAR (Mouth Aspect Ratio) hesaplamaları yapılır.

Buradaki kritik nokta: YOLO'nun tespiti olmadan EAR ve MAR hesaplanamaz. Önce yapay zeka "bakılacak yeri" söyler, sonra matematik "durumu" analiz eder.

4. **Karar Mekanizması:**

- Eğer EAR değeri belirli bir eşiğin (threshold) altına düşerse -> Göz Kapalı.

- Eğer MAR değeri belirli bir oranın üstüne çıkarsa -> Esneme.

5. **Uyarı:** Bu durumlar belirli bir süre (frame sayısı) boyunca devam ederse sistem sesli alarm verir.

## Yapılacaklar (To-Do) & Geliştirme Fikirleri
Bu proje şu an çalışır durumda ama geliştirmeye her zaman açık. İleride şunları eklemeyi düşünüyorum:
* [ ] Gece sürüşü için düşük ışık iyileştirmeleri.
* [ ] Sadece yorgunluk değil, telefonla konuşma gibi dikkat dağıtıcı unsurların tespiti.
* [ ] Mobil uygulama entegrasyonu.

*Bu projeyi Bilgisayar Mühendisliği bitirme projesi kapsamında geliştirdim.*
