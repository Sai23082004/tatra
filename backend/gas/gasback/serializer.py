from rest_framework import serializers
from django.contrib.auth.models import User
from .models import UserProfile
from .models import *

class GasSensorSerializer(serializers.ModelSerializer):
    class Meta:
        model = GasSensor
        fields = '__all__'
        read_only_fields = ['user']

class GasReadingSerializer(serializers.ModelSerializer):
    sensor_name = serializers.CharField(source='sensor.sensor_name', read_only=True)
    location = serializers.CharField(source='sensor.location', read_only=True)
    
    class Meta:
        model = GasReading
        fields = '__all__'

class GasAlertSerializer(serializers.ModelSerializer):
    class Meta:
        model = GasAlert
        fields = '__all__'
        read_only_fields = ['user']

class PipelineSectionSerializer(serializers.ModelSerializer):
    class Meta:
        model = PipelineSection
        fields = '__all__'
        read_only_fields = ['user']

class MaintenanceScheduleSerializer(serializers.ModelSerializer):
    class Meta:
        model = MaintenanceSchedule
        fields = '__all__'
        read_only_fields = ['user']

class GasRegulatorSerializer(serializers.ModelSerializer):
    class Meta:
        model = GasRegulator
        fields = '__all__'
        read_only_fields = ['user']

class EmergencyContactSerializer(serializers.ModelSerializer):
    class Meta:
        model = EmergencyContact
        fields = '__all__'
        read_only_fields = ['user']

class UserProfileUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = ['phone_number', 'profile_image', 'notification_preferences']

class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, required=True, style={'input_type': 'password'})
    device_unique_code = serializers.CharField(required=True)
    email = serializers.EmailField(required=True)
    
    class Meta:
        model = User
        fields = ['username', 'email', 'password', 'device_unique_code']

    def validate_email(self, value):
        if User.objects.filter(email=value).exists():
            raise serializers.ValidationError("A user with this email already exists.")
        return value

    def create(self, validated_data):
        device_unique_code = validated_data.pop('device_unique_code')
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password']
        )
        UserProfile.objects.create(user=user, device_unique_code=device_unique_code)
        return user

from django.contrib.auth import authenticate

class LoginSerializer(serializers.Serializer):
    email = serializers.EmailField(required=True)
    password = serializers.CharField(write_only=True, required=True, style={'input_type': 'password'})

    def validate(self, data):
        email = data.get('email')
        password = data.get('password')

        if email and password:
            # Use filter().first() instead of get() to avoid MultipleObjectsReturned
            user = User.objects.filter(email=email).first()
            if not user:
                raise serializers.ValidationError("Invalid email or password.")

            user = authenticate(username=user.username, password=password)
            if user:
                if not user.is_active:
                    raise serializers.ValidationError("User account is disabled.")
                return user
            else:
                raise serializers.ValidationError("Invalid email or password.")
        else:
            raise serializers.ValidationError("Both email and password are required.")
