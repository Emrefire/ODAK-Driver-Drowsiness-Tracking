using Microsoft.EntityFrameworkCore;

namespace DriverDrowWeb.Models
{
    public class DriverContext : DbContext
    {
        public DriverContext(DbContextOptions<DriverContext> options) : base(options)
        {
        }

        public DbSet<User> Users { get; set; }
        public DbSet<DrivingSession> DrivingSessions { get; set; }
    }
}