"""
API endpoints étendus pour les chauffeurs (ravitaillement et entretien)
À importer dans core/views.py ou utiliser comme module séparé
"""
from django.shortcuts import get_object_or_404
from django.http import JsonResponse
from django.db.models import Q
from django.utils import timezone
from core.models import Utilisateur, Vehicule
from ravitaillement.models import Ravitaillement, Station
from entretien.models import Entretien
import datetime

def driver_fuel_list(request):
    """API pour récupérer la liste des ravitaillements du chauffeur"""
    if request.method == 'GET':
        try:
            # Pour les tests, permettre l'accès sans authentification
            user_id = request.session.get('user_id')
            if not user_id:
                # Pour test : utiliser le premier chauffeur disponible
                try:
                    user = Utilisateur.objects.filter(role='chauffeur').first()
                    if not user:
                        return JsonResponse({'error': 'Aucun chauffeur trouvé', 'success': False}, status=404)
                except:
                    return JsonResponse({'error': 'Erreur de base de données', 'success': False}, status=500)
            else:
                try:
                    user = Utilisateur.objects.get(id=user_id)
                    if user.role != 'chauffeur':
                        return JsonResponse({'error': 'Accès non autorisé', 'success': False}, status=403)
                except Utilisateur.DoesNotExist:
                    return JsonResponse({'error': 'Utilisateur non trouvé', 'success': False}, status=404)
            
            # Récupérer les ravitaillements où le chauffeur est impliqué
            ravitaillements = Ravitaillement.objects.filter(
                Q(chauffeur=user) | Q(createur=user)
            ).select_related('vehicule', 'station').order_by('-date_ravitaillement')
            
            data = []
            for rav in ravitaillements:
                data.append({
                    'id': rav.id,
                    'vehicule': f"{rav.vehicule.immatriculation} - {rav.vehicule.marque} {rav.vehicule.modele}",
                    'date_ravitaillement': rav.date_ravitaillement.strftime('%d/%m/%Y %H:%M'),
                    'station': rav.station.nom if rav.station else rav.nom_station,
                    'kilometrage_avant': rav.kilometrage_avant,
                    'kilometrage_apres': rav.kilometrage_apres,
                    'litres': float(rav.litres),
                    'cout_total': float(rav.cout_total),
                    'consommation': rav.consommation_moyenne_reelle
                })
            
            return JsonResponse({'success': True, 'data': data})
            
        except Exception as e:
            return JsonResponse({'error': str(e), 'success': False}, status=500)
    
    return JsonResponse({'error': 'Method not allowed', 'success': False}, status=405)

def driver_fuel_create(request):
    """API pour créer un ravitaillement"""
    if request.method == 'POST':
        token = request.headers.get('Authorization', '').replace('Bearer ', '')
        if not token or token != request.session.get('mobile_token', ''):
            return JsonResponse({'error': 'Token invalide', 'success': False}, status=401)
        
        user_id = request.session.get('user_id')
        try:
            user = Utilisateur.objects.get(id=user_id)
            if user.role != 'chauffeur':
                return JsonResponse({'error': 'Accès non autorisé', 'success': False}, status=403)
            
            # Récupérer les données
            vehicule_id = request.POST.get('vehicule_id')
            station_id = request.POST.get('station_id')
            nom_station = request.POST.get('nom_station')
            kilometrage_avant = request.POST.get('kilometrage_avant')
            kilometrage_apres = request.POST.get('kilometrage_apres')
            litres = request.POST.get('litres')
            cout_unitaire = request.POST.get('cout_unitaire')
            
            if not all([vehicule_id, kilometrage_apres, litres, cout_unitaire]):
                return JsonResponse({'error': 'Champs obligatoires manquants', 'success': False}, status=400)
            
            vehicule = Vehicule.objects.get(id=vehicule_id)
            
            # Créer le ravitaillement
            ravitaillement = Ravitaillement.objects.create(
                vehicule=vehicule,
                station=Station.objects.get(id=station_id) if station_id else None,
                nom_station=nom_station if not station_id else None,
                kilometrage_avant=int(kilometrage_avant) if kilometrage_avant else 0,
                kilometrage_apres=int(kilometrage_apres),
                litres=float(litres),
                cout_unitaire=float(cout_unitaire),
                createur=user,
                chauffeur=user
            )
            
            return JsonResponse({
                'success': True, 
                'message': 'Ravitaillement enregistré avec succès',
                'id': ravitaillement.id
            })
            
        except Vehicule.DoesNotExist:
            return JsonResponse({'error': 'Véhicule non trouvé', 'success': False}, status=404)
        except Exception as e:
            return JsonResponse({'error': str(e), 'success': False}, status=500)
    
    return JsonResponse({'error': 'Method not allowed', 'success': False}, status=405)

def driver_maintenance_list(request):
    """API pour récupérer la liste des entretiens du chauffeur"""
    if request.method == 'GET':
        try:
            # Pour les tests, permettre l'accès sans authentification
            user_id = request.session.get('user_id')
            if not user_id:
                # Pour test : utiliser le premier chauffeur disponible
                try:
                    user = Utilisateur.objects.filter(role='chauffeur').first()
                    if not user:
                        return JsonResponse({'error': 'Aucun chauffeur trouvé', 'success': False}, status=404)
                except:
                    return JsonResponse({'error': 'Erreur de base de données', 'success': False}, status=500)
            else:
                try:
                    user = Utilisateur.objects.get(id=user_id)
                    if user.role != 'chauffeur':
                        return JsonResponse({'error': 'Accès non autorisé', 'success': False}, status=403)
                except Utilisateur.DoesNotExist:
                    return JsonResponse({'error': 'Utilisateur non trouvé', 'success': False}, status=404)
            
            # Récupérer les entretiens créés par le chauffeur
            entretiens = Entretien.objects.filter(
                createur=user
            ).select_related('vehicule').order_by('-date_creation')
            
            data = []
            for ent in entretiens:
                data.append({
                    'id': ent.id,
                    'vehicule': f"{ent.vehicule.immatriculation} - {ent.vehicule.marque} {ent.vehicule.modele}",
                    'date_entretien': ent.date_entretien.strftime('%d/%m/%Y'),
                    'type_entretien': ent.get_type_entretien_display(),
                    'statut': ent.get_statut_display(),
                    'garage': ent.garage,
                    'motif': ent.motif,
                    'cout': float(ent.cout),
                    'kilometrage': ent.kilometrage
                })
            
            return JsonResponse({'success': True, 'data': data})
            
        except Exception as e:
            return JsonResponse({'error': str(e), 'success': False}, status=500)
    
    return JsonResponse({'error': 'Method not allowed', 'success': False}, status=405)

def driver_maintenance_create(request):
    """API pour créer un entretien"""
    if request.method == 'POST':
        token = request.headers.get('Authorization', '').replace('Bearer ', '')
        if not token or token != request.session.get('mobile_token', ''):
            return JsonResponse({'error': 'Token invalide', 'success': False}, status=401)
        
        user_id = request.session.get('user_id')
        try:
            user = Utilisateur.objects.get(id=user_id)
            if user.role != 'chauffeur':
                return JsonResponse({'error': 'Accès non autorisé', 'success': False}, status=403)
            
            # Récupérer les données
            vehicule_id = request.POST.get('vehicule_id')
            type_entretien = request.POST.get('type_entretien')
            date_entretien = request.POST.get('date_entretien')
            garage = request.POST.get('garage')
            motif = request.POST.get('motif')
            cout = request.POST.get('cout')
            kilometrage = request.POST.get('kilometrage')
            
            if not all([vehicule_id, type_entretien, date_entretien, garage, motif, cout, kilometrage]):
                return JsonResponse({'error': 'Champs obligatoires manquants', 'success': False}, status=400)
            
            vehicule = Vehicule.objects.get(id=vehicule_id)
            
            # Créer l'entretien
            entretien = Entretien.objects.create(
                vehicule=vehicule,
                type_entretien=type_entretien,
                date_entretien=datetime.datetime.strptime(date_entretien, '%Y-%m-%d').date(),
                garage=garage,
                motif=motif,
                cout=float(cout),
                kilometrage=int(kilometrage),
                createur=user
            )
            
            return JsonResponse({
                'success': True, 
                'message': 'Entretien enregistré avec succès',
                'id': entretien.id
            })
            
        except Vehicule.DoesNotExist:
            return JsonResponse({'error': 'Véhicule non trouvé', 'success': False}, status=404)
        except Exception as e:
            return JsonResponse({'error': str(e), 'success': False}, status=500)
    
    return JsonResponse({'error': 'Method not allowed', 'success': False}, status=405)

def driver_vehicles_list(request):
    """API pour récupérer la liste des véhicules disponibles pour le chauffeur"""
    if request.method == 'GET':
        try:
            # Pour les tests, permettre l'accès sans authentification
            # Récupérer les véhicules actifs
            vehicles = Vehicule.objects.filter(
                actif=True
            ).order_by('immatriculation')
            
            data = []
            for vehicle in vehicles:
                data.append({
                    'id': vehicle.id,
                    'immatriculation': vehicle.immatriculation,
                    'marque': vehicle.marque,
                    'modele': vehicle.modele,
                    'kilometrage_actuel': vehicle.kilometrage_actuel or 0,
                    'consommation_moyenne': vehicle.consommation_moyenne or 0
                })
            
            return JsonResponse({'success': True, 'data': data})
            
        except Exception as e:
            return JsonResponse({'error': str(e), 'success': False}, status=500)
    
    return JsonResponse({'error': 'Method not allowed', 'success': False}, status=405)

def driver_stations_list(request):
    """API pour récupérer la liste des stations de ravitaillement"""
    if request.method == 'GET':
        try:
            # Pour les tests, permettre l'accès sans authentification
            # Récupérer les stations actives
            stations = Station.objects.filter(
                est_active=True
            ).order_by('nom')
            
            data = []
            for station in stations:
                data.append({
                    'id': station.id,
                    'nom': station.nom,
                    'adresse': station.adresse,
                    'ville': station.ville
                })
            
            return JsonResponse({'success': True, 'data': data})
            
        except Exception as e:
            return JsonResponse({'error': str(e), 'success': False}, status=500)
    
    return JsonResponse({'error': 'Method not allowed', 'success': False}, status=405)
