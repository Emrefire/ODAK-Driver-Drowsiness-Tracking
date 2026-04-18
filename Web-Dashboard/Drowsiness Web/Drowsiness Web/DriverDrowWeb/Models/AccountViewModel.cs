using System.ComponentModel.DataAnnotations;

namespace DriverDrowWeb.Models
{
    public class LoginViewModel
    {
        [Required(ErrorMessage = "E-posta adresi gereklidir.")]
        [EmailAddress]
        public string Email { get; set; }

        [Required(ErrorMessage = "Şifre gereklidir.")]
        [DataType(DataType.Password)]
        public string Password { get; set; }
    }

    public class RegisterViewModel
    {
        [Required(ErrorMessage = "Ad Soyad gereklidir.")]
        public string FullName { get; set; }

        [Required(ErrorMessage = "E-posta gereklidir.")]
        [EmailAddress]
        public string Email { get; set; }

        [Required(ErrorMessage = "Şifre gereklidir.")]
        [DataType(DataType.Password)]
        public string Password { get; set; }

        [Compare("Password", ErrorMessage = "Şifreler uyuşmuyor.")]
        [DataType(DataType.Password)]
        public string ConfirmPassword { get; set; }
    }
}