using System;
using System.ComponentModel.DataAnnotations;

namespace DriverDrowWeb.Models
{
    public class DrivingSession
    {
        [Key]
        public int Id { get; set; }
        public int UserId { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public int AverageFatigue { get; set; }
        public int SleepDuration { get; set; }
        public int YawnCount { get; set; }
        public int Score { get; set; }
    }
}