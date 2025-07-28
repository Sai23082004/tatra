const express = require('express');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

// Get dashboard data
router.get('/dashboard', authMiddleware, async (req, res) => {
  try {
    // Mock dashboard data (replace with real sensor data)
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
      ],
      lastUpdated: new Date().toISOString()
    };

    res.json({
      success: true,
      data: dashboardData,
      user: req.user.username
    });

  } catch (error) {
    console.error('Dashboard error:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching dashboard data'
    });
  }
});

// Get gas leak detection data
router.get('/gas-leak', authMiddleware, async (req, res) => {
  try {
    const gasLeakData = {
      status: "Normal",
      level: "Safe",
      lastChecked: new Date().toISOString(),
      sensors: [
        {
          id: 1,
          name: "Kitchen Sensor",
          status: "Normal",
          value: "0 ppm",
          location: "Kitchen",
          battery: "95%"
        },
        {
          id: 2,
          name: "Living Room Sensor",
          status: "Normal",
          value: "0 ppm",
          location: "Living Room",
          battery: "87%"
        },
        {
          id: 3,
          name: "Basement Sensor",
          status: "Normal",
          value: "0 ppm",
          location: "Basement",
          battery: "92%"
        }
      ]
    };

    res.json({
      success: true,
      data: gasLeakData
    });

  } catch (error) {
    console.error('Gas leak data error:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching gas leak data'
    });
  }
});

module.exports = router;
