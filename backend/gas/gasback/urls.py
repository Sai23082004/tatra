from django.urls import path
from . import views

urlpatterns = [
    # Authentication
    path('auth/register/', views.RegisterView.as_view(), name='register'),
    path('auth/login/', views.LoginView.as_view(), name='login'),
    path('simple/', views.SimpleView.as_view(), name='simple'),
# Dashboard
    path('dashboard/', views.dashboard_data, name='dashboard'),
    
    # Gas Leak Detection
    path('gas-leak/status/', views.gas_leak_status, name='gas_leak_status'),
    path('gas-leak/scan/', views.trigger_gas_scan, name='trigger_gas_scan'),
    
    # Gas Level Monitoring
    path('gas-level/data/', views.gas_level_data, name='gas_level_data'),
    
    # Pipeline Health
    path('pipeline/health/', views.pipeline_health_data, name='pipeline_health'),
    
    # Regulator Control
    path('regulator/control/', views.regulator_control, name='regulator_control'),
    
    # Emergency Contacts
    path('emergency/contacts/', views.emergency_contacts, name='emergency_contacts'),
    path('emergency/contacts/', views.emergency_contacts_management, name='emergency_contacts'),
    path('emergency/contacts/<int:contact_id>/delete/', views.delete_emergency_contact, name='delete_contact'),
    path('emergency/sos/', views.trigger_emergency_sos, name='emergency_sos'),
    
    # Profile Management
    path('profile/', views.user_profile_management, name='profile_management'),
    path('profile/upload-image/', views.upload_profile_image, name='upload_profile_image'),
]
