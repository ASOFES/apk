from django.urls import path
from . import views
from driver_api import driver_course_history_api

urlpatterns = [
    path('', views.home_view, name='home'),
    path('login/', views.login_view, name='login'),
    path('logout/', views.logout_view, name='logout'),
    path('profile/', views.profile_view, name='profile'),
    path('profile/edit/', views.edit_profile, name='edit_profile'),
    
    # API Mobile - Login sans CSRF
    path('login-api/', views.login_api_mobile, name='login_api_mobile'),
    
    # API endpoints pour chauffeurs
    path('api/driver/assigned-course/', views.driver_assigned_course, name='driver_assigned_course'),
    path('api/driver/course-history/', views.driver_course_history, name='driver_course_history'),
    path('api/driver/course-history-real/', driver_course_history_api, name='driver_course_history_api'),
    path('api/driver/course/<int:course_id>/start/', views.driver_start_course, name='driver_start_course'),
    path('api/driver/course/<int:course_id>/complete/', views.driver_complete_course, name='driver_complete_course'),
    
    # API endpoints pour ravitaillement et entretien
    # path('api/driver/fuel/', driver_fuel_list, name='driver_fuel_list'),
    # path('api/driver/fuel/create/', driver_fuel_create, name='driver_fuel_create'),
    # path('api/driver/maintenance/', driver_maintenance_list, name='driver_maintenance_list'),
    # path('api/driver/maintenance/create/', driver_maintenance_create, name='driver_maintenance_create'),
    # path('api/driver/vehicles/', driver_vehicles_list, name='driver_vehicles_list'),
    # path('api/driver/stations/', driver_stations_list, name='driver_stations_list'),
    
    # Gestion des utilisateurs
    path('users/', views.user_list, name='user_list'),
    path('users/create/', views.user_create, name='user_create'),
    path('users/<int:pk>/edit/', views.user_edit, name='user_edit'),
    path('users/<int:pk>/password-reset/', views.user_password_reset, name='user_password_reset'),
    path('users/<int:pk>/toggle-active/', views.user_toggle_active, name='user_toggle_active'),
    path('users/<int:pk>/delete/', views.user_delete, name='user_delete'),
    path('users/<int:pk>/change-departement/', views.user_change_departement, name='user_change_departement'),
    path('users/export/pdf/', views.user_list_pdf, name='user_list_pdf'),
    path('users/export/excel/', views.user_list_excel, name='user_list_excel'),
    
    # Gestion des véhicules
    path('vehicules/', views.vehicule_list, name='vehicule_list'),
    path('vehicules/create/', views.vehicule_create, name='vehicule_create'),
    path('vehicules/<int:pk>/', views.vehicule_detail, name='vehicule_detail'),
    path('vehicules/<int:pk>/edit/', views.vehicule_edit, name='vehicule_edit'),
    path('vehicules/<int:pk>/delete/', views.vehicule_delete, name='vehicule_delete'),
    
    # Export PDF des véhicules
    path('vehicules/export/pdf/', views.vehicule_list_pdf, name='vehicule_list_pdf'),
    path('vehicules/<int:pk>/export/pdf/', views.vehicule_detail_pdf, name='vehicule_detail_pdf'),
    path('vehicules/export/excel/', views.vehicule_list_excel, name='vehicule_list_excel'),
    path('etablissement/create/', views.create_etablissement, name='create_etablissement'),
    path('users/create/', views.create_user, name='create_user'),
    path('choose-etablissement/', views.choose_etablissement, name='choose_etablissement'),
    path('send-test-sms/', views.send_test_sms, name='send_test_sms'),
    path('send-test-sms-africastalking/', views.send_test_sms_africastalking, name='send_test_sms_africastalking'),
    # Contrôle de l'application
    path('application-control/password/', views.application_control_password, name='application_control_password'),
    path('application-control/', views.application_control, name='application_control'),
    path('application-blocked/', views.application_blocked, name='application_blocked'),
    path('application-control/logout/', views.application_control_logout, name='application_control_logout'),
    # Gestion des départements
    path('departements/', views.departement_list, name='departement_list'),
    path('departements/create/', views.departement_create, name='departement_create'),
    path('departements/<int:pk>/', views.departement_detail, name='departement_detail'),
    path('departements/<int:pk>/edit/', views.departement_edit, name='departement_edit'),
    path('messagerie/send/', views.send_message, name='send_message'),
    path('messagerie/messages/', views.get_messages, name='get_messages'),
    path('messagerie/users/', views.get_users, name='get_users'),
    path('messagerie/unread_status/', views.get_unread_messages_status, name='get_unread_messages_status'),
    path('vehicule/<int:vehicule_id>/changer-etablissement/', views.vehicule_change_etablissement, name='vehicule_change_etablissement'),
    path('configuration/', views.configuration_view, name='configuration'),
]
