"""
URL configuration for gestion_vehicules project.
"""
from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include('core.urls')),
    path('chauffeur/', include('chauffeur.urls')),
    path('dispatch/', include('dispatch.urls')),
    path('demandeur/', include('demandeur.urls')),
    path('entretien/', include('entretien.urls')),
    path('ravitaillement/', include('ravitaillement.urls')),
]
