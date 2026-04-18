using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using DriverDrowWeb.Models;
using System.Security.Cryptography;
using System.Text;
using Microsoft.AspNetCore.SignalR;
using DriverDrowWeb.Hubs;

namespace DriverDrowWeb.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class MobileApiController : ControllerBase
    {
        private readonly DriverContext _context;
        private readonly IHubContext<DriverHub> _hubContext;

        // Constructor: Hem veritabanını hem de SignalR Hub'ını içeri alıyoruz
        public MobileApiController(DriverContext context, IHubContext<DriverHub> hubContext)
        {
            _context = context;
            _hubContext = hubContext;
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterViewModel model)
        {
            // Breakpoint'i tam buraya (süslü parantezin içine) koy!
            if (model == null)
                return Ok(new { success = false, message = "Model boş geldi!" });

            // Eğer model kurallara uymuyorsa (Örn: şifre kısa), hatayı içeri sokmak için:
            if (!ModelState.IsValid)
            {
                var errors = string.Join(" | ", ModelState.Values.SelectMany(v => v.Errors).Select(e => e.ErrorMessage));
                return Ok(new { success = false, message = "Validation Hatası: " + errors });
            }

            if (await _context.Users.AnyAsync(x => x.Email == model.Email))
            {
                return Ok(new { success = false, message = "Bu e-posta adresi zaten kullanımda." });
            }

            var user = new User
            {
                FullName = model.FullName,
                Email = model.Email,
                PasswordHash = HashPassword(model.Password),
                CreatedAt = DateTime.Now
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return Ok(new { success = true, message = "Kayıt başarıyla oluşturuldu." });
        }
        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginViewModel model)
        {
            // 1. Android'den veri hiç gelemediyse:
            if (model == null || string.IsNullOrEmpty(model.Email))
                return Ok(new { success = false, message = "Android'den veri boş geldi! JSON uyuşmazlığı." });

            string hashedInput = HashPassword(model.Password);

            // 2. Önce sadece E-postayı veritabanında arayalım:
            var user = _context.Users.FirstOrDefault(x => x.Email == model.Email);

            if (user == null)
            {
                // Eğer e-posta yoksa telefona tam olarak ne aradığını göndersin:
                return Ok(new { success = false, message = $"E-posta yanlış. Aranan: '{model.Email}'" });
            }

            // 3. E-posta bulundu ama şifre uyuşmuyor:
            if (user.PasswordHash != hashedInput)
            {
                return Ok(new { success = false, message = $"Şifre yanlış! API Hash: '{hashedInput}'" });
            }

            // 4. Her şey doğruysa içeri al!
            return Ok(new { success = true, userId = user.Id, fullName = user.FullName });
        }

        // 2. SÜRÜŞ VERİSİ KAYDETME (Sürüş Bittiğinde Çağrılır)
        [HttpPost("save-session")]
        public async Task<IActionResult> SaveSession([FromBody] DrivingSession session)
        {
            if (session == null) return BadRequest();

            try 
            {
                // Zaman dilimi düzeltmesi (İsteğe bağlı, DashboardController'daki gibi +3 saat yapabilirsin)
                if(session.StartTime == default) session.StartTime = DateTime.Now;
                session.EndTime = DateTime.Now;

                _context.DrivingSessions.Add(session);
                await _context.SaveChangesAsync();
                
                return Ok(new { success = true, message = "Sürüş veritabanına kaydedildi." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Kaydetme hatası: " + ex.Message });
            }
        }

        // 3. CANLI VERİ AKIŞI (Sürüş Sırasında Dashboard'u Günceller)
        // Mobil uygulama her 5 saniyede bir buraya veri atarsa Web Dashboard anlık değişir
        [HttpPost("live-update")]
        public async Task<IActionResult> LiveUpdate([FromBody] DriverMetrics metrics)
        {
            if (metrics == null) return BadRequest();

            try 
            {
                // SignalR üzerinden Web tarafındaki tüm bağlı istemcilere "ReceiveMetrics" mesajını fırlatıyoruz
                await _hubContext.Clients.All.SendAsync("ReceiveMetrics", metrics);
                return Ok(new { success = true, message = "Canlı veri dashboard'a iletildi." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "SignalR hatası: " + ex.Message });
            }
        }

        // 4. GEÇMİŞ SÜRÜŞLERİ LİSTELEME
        [HttpGet("history/{userId}")]
        public async Task<IActionResult> GetHistory(int userId)
        {
            var data = await _context.DrivingSessions
                .Where(x => x.UserId == userId)
                .OrderByDescending(x => x.StartTime)
                .ToListAsync();
                
            return Ok(data);
        }

        // Şifreleme Metodu (Web Girişi ile aynı sonucu vermesi için)
        private string HashPassword(string password)
        {
            using (var sha256 = SHA256.Create())
            {
                var hashedBytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
                return BitConverter.ToString(hashedBytes).Replace("-", "").ToLower();
            }
        }
    }
}