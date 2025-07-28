const express = require('express');
const router = express.Router();

// Get dashboard data
router.get('/dashboard', async (req, res) => {
  try {
    const dashboardData = {
      statusData: [
        {
          title: "Gas Level",
          value: "85%",
          icon: "LocalGasStation",
          color: "#4CAF50",
          isHealthy: true
        },
        {
          title: "Pressure",
          value: "Normal",
          icon: "Speed",
          color: "#2196F3",
          isHealthy: true
        },
        {
          title: "Temperature",
          value: "24°C",
          icon: "Thermostat",
          color: "#FF9800",
          isHealthy: true
        },
        {
          title: "Safety",
          value: "Secure",
          icon: "Shield",
          color: "#4CAF50",
          isHealthy: true
        }
      ],
      recentActivity: [
        {
          icon: "CheckCircle",
          title: "Gas level check completed",
          time: "2 minutes ago",
          color: "#4CAF50"
        },
        {
          icon: "Settings",
          title: "Regulator settings updated",
          time: "1 hour ago",
          color: "#2196F3"
        },
        {
          icon: "Warning",
          title: "Pressure sensor calibrated",
          time: "3 hours ago",
          color: "#FF9800"
        }
      ]
    };

    res.json({
      success: true,
      data: dashboardData
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

module.exports = router;
