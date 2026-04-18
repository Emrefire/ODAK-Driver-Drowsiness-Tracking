using Microsoft.EntityFrameworkCore;
using DriverDrowWeb.Models;
using DriverDrowWeb.Hubs;
using Microsoft.AspNetCore.Authentication.Cookies;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<DriverContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

// Kimlik Doğrulama
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.LoginPath = "/Account/Login"; // Giriş yapmamış kişi buraya yönlendirilsin
        options.ExpireTimeSpan = TimeSpan.FromMinutes(60); // Oturum 60 dk sürsün
    });

builder.Services.AddControllersWithViews();
builder.Services.AddControllers();
builder.Services.AddSignalR();

// CORS Politikası (Python ve dış kaynaklar için)
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowPython", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});

var app = builder.Build();

// Hata Yönetimi ve HTTPS
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

//app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();
app.UseCors("AllowPython");

// Kimlik Doğrulama Sıralaması
app.UseAuthentication(); 
app.UseAuthorization();

app.MapControllers();
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.MapHub<DriverHub>("/driverHub");

app.Run();