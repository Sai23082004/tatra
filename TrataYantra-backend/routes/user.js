const express = require('express');
const router = express.Router();

// Get user profile
router.get('/profile', async (req, res) => {
  try {
    res.json({
      success: true,
      user: {
        id: 'mock-user-id',
        username: 'Test User',
        email: 'test@example.com'
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Error fetching user profile'
    });
  }
});

module.exports = router;
