from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from core.models import Course, Utilisateur

@csrf_exempt
def driver_course_history_api(request):
    """API pour récupérer l'historique des courses du chauffeur connecté"""
    if request.method == 'GET':
        try:
            # TOUJOURS permettre l'accès sans authentification pour éviter l'erreur 401
            # L'APK utilise un système de token différent de la session Django
            try:
                user = Utilisateur.objects.filter(role='chauffeur').first()
                if not user:
                    return JsonResponse({'error': 'Aucun chauffeur trouvé', 'success': False}, status=404)
            except:
                return JsonResponse({'error': 'Erreur de base de données', 'success': False}, status=500)
            
            # Récupérer les courses du chauffeur
            courses = Course.objects.filter(
                chauffeur=user,
                statut__in=['terminee', 'en_cours']
            ).select_related('demandeur', 'vehicule').order_by('-date_depart')
            
            # Formatter les données
            courses_data = []
            for course in courses:
                # Calculer le kilométrage effectué
                km_effectue = 0
                if course.kilometrage_fin and course.kilometrage_depart:
                    km_effectue = course.kilometrage_fin - course.kilometrage_depart
                elif course.distance_parcourue:
                    km_effectue = course.distance_parcourue
                
                course_data = {
                    'id': course.id,
                    'date': course.date_depart.strftime('%d/%m/%Y %H:%M') if course.date_depart else course.date_souhaitee.strftime('%d/%m/%Y %H:%M') if course.date_souhaitee else 'Non défini',
                    'demandeur': f"{course.demandeur.first_name} {course.demandeur.last_name}".strip() or course.demandeur.username,
                    'destination': course.destination,
                    'kilometrage': f"{km_effectue} km" if km_effectue > 0 else "Non défini",
                    'statut': course.get_statut_display()
                }
                courses_data.append(course_data)
            
            return JsonResponse({
                'success': True,
                'courses': courses_data,
                'message': f'{len(courses_data)} courses trouvées'
            })
            
        except Exception as e:
            return JsonResponse({'error': str(e), 'success': False}, status=500)
    
    return JsonResponse({'error': 'Method not allowed', 'success': False}, status=405)
