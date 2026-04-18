@echo off
echo HTTPS sertifikası oluşturuluyor...

REM Private key oluştur
openssl genrsa -out key.pem 2048

REM CSR oluştur
openssl req -new -key key.pem -out csr.pem -subj "/C=TR/ST=Istanbul/L=Istanbul/O=DriverDrowsiness/CN=localhost"

REM Self-signed sertifika oluştur
openssl x509 -req -days 365 -in csr.pem -signkey key.pem -out cert.pem

echo ✅ Sertifikalar oluşturuldu!
echo 📄 key.pem - Private Key
echo 📄 cert.pem - Certificate
pause
