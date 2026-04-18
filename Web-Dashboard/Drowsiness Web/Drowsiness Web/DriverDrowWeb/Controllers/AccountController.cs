using Microsoft.AspNetCore.Mvc;
using DriverDrowWeb.Models;
using System.Security.Cryptography;
using System.Text;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using System.Security.Claims;

namespace DriverDrowWeb.Controllers
{
    public class AccountController : Controller
    {
        private readonly DriverContext _context;

        public AccountController(DriverContext context)
        {
            _context = context;
        }

        [HttpGet]
        public IActionResult Login()
        {
            if (User.Identity.IsAuthenticated)
            {
                return RedirectToAction("Index", "Dashboard");
            }
            return View();
        }

        [HttpPost]
        public async Task<IActionResult> Login(LoginViewModel model)
        {
            if (ModelState.IsValid)
            {
                string hashedPassword = HashPassword(model.Password);

                var user = _context.Users.FirstOrDefault(x => x.Email == model.Email && x.PasswordHash == hashedPassword);

                if (user != null)
                {
                    var claims = new List<Claim>
                    {
                        new Claim(ClaimTypes.Name, user.FullName), // Adı
                        new Claim(ClaimTypes.Email, user.Email),   // E-postası
                        new Claim("UserId", user.Id.ToString())    // ID'si
                    };

                    var userIdentity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
                    var authProperties = new AuthenticationProperties();

                    await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(userIdentity), authProperties);
                    return RedirectToAction("Index", "Dashboard");
                }
                else
                {
                    ViewBag.Error = "E-posta veya şifre hatalı!";
                }
            }
            return View(model);
        }

        [HttpGet]
        public IActionResult Register()
        {
            if (User.Identity.IsAuthenticated)
            {
                return RedirectToAction("Index", "Home");
            }
            return View();
        }

        [HttpPost]
        public IActionResult Register(RegisterViewModel model)
        {
            if (ModelState.IsValid)
            {
                if (_context.Users.Any(x => x.Email == model.Email))
                {
                    ModelState.AddModelError("Email", "Bu e-posta adresi zaten kayıtlı.");
                    return View(model);
                }

                var newUser = new User
                {
                    FullName = model.FullName,
                    Email = model.Email,
                    PasswordHash = HashPassword(model.Password),
                    CreatedAt = DateTime.Now
                };

                _context.Users.Add(newUser);
                _context.SaveChanges();

                TempData["SuccessMessage"] = "Kayıt başarılı! Lütfen giriş yapınız.";
                return RedirectToAction("Login");
            }
            return View(model);
        }
        public async Task<IActionResult> Logout()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            return RedirectToAction("Login");
        }

        // YARDIMCI METOD
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