using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR; 
using Microsoft.AspNetCore.Authorization;
using DriverDrowWeb.Hubs; 
using DriverDrowWeb.Models; 
using System.Security.Claims; 
using System.Linq;

namespace DriverDrowWeb.Controllers
{
    [Authorize] 
    public class DashboardController : Controller
    {
        private readonly DriverContext _context;      
        private readonly IHubContext<DriverHub> _hubContext;

        public DashboardController(DriverContext context, IHubContext<DriverHub> hubContext)
        {
            _context = context;
            _hubContext = hubContext;
        }

        public IActionResult Index()
        {
            ViewBag.UserName = User.Identity.Name;

            var userIdClaim = User.FindFirst("UserId");
            int userId = userIdClaim != null ? int.Parse(userIdClaim.Value) : 0;

            var history = _context.DrivingSessions
                                          .Where(x => x.UserId == userId)
                                          .OrderByDescending(x => x.StartTime)
                                          .ToList();
            return View("Dashboard", history); 
        }

        [HttpPost]
        [AllowAnonymous]
        public async Task<IActionResult> UpdateMetrics([FromBody] DriverMetrics metrics)
        {
            if (metrics == null) return BadRequest();

            await _hubContext.Clients.All.SendAsync("ReceiveMetrics", metrics);

            return Ok(new { message = "Veri başarıyla Dashboard'a iletildi" });
        }

        [HttpPost]
        public IActionResult SaveDrive([FromBody] DrivingSession data)
        {
            try
            {
                if (data == null)
                {
                    return Json(new { success = false, message = "Veri sunucuya ulaşmadı." });
                }

                var userIdClaim = User.FindFirst("UserId");
                if (userIdClaim == null)
                {
                    return Json(new { success = false, message = "Oturum süreniz dolmuş." });
                }

                int userId = int.Parse(userIdClaim.Value);
                data.UserId = userId;

                data.StartTime = data.StartTime.AddHours(3);
                data.EndTime = DateTime.UtcNow.AddHours(3);

                _context.DrivingSessions.Add(data);
                _context.SaveChanges();

                return Json(new { success = true });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, message = "Sunucu Hatası: " + ex.Message });
            }
        }

        [HttpPost]
        public IActionResult DeleteDrive(int id)
        {
            try
            {
                var userIdClaim = User.FindFirst("UserId");
                if (userIdClaim == null) return Json(new { success = false, message = "Oturum bulunamadı." });

                int userId = int.Parse(userIdClaim.Value);

                var record = _context.DrivingSessions.FirstOrDefault(x => x.Id == id && x.UserId == userId);

                if (record != null)
                {
                    _context.DrivingSessions.Remove(record);
                    _context.SaveChanges();
                    return Json(new { success = true });
                }
                else
                {
                    return Json(new { success = false, message = "Kayıt bulunamadı veya silme yetkiniz yok." });
                }
            }
            catch (Exception ex)
            {
                return Json(new { success = false, message = "Hata: " + ex.Message });
            }
        }
    }

    public class DriverMetrics
    {
        public int CurrentFatigue { get; set; }
        public double SleepTime { get; set; }
        public int YawnCount { get; set; }
        public int Score { get; set; }
        public string Status { get; set; }
    }
}