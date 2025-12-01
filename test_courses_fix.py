#!/usr/bin/env python
"""
Script de test pour vérifier que les courses assignées apparaissent correctement
dans le dashboard du chauffeur, même quand elles sont en statut 'en_attente'.
"""

import os
import sys
import django

# Configuration de Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'settings_base')
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

try:
    django.setup()
except Exception as e:
    print(f"Erreur lors de la configuration Django: {e}")
    sys.exit(1)

from core.models import Utilisateur, Course, Vehicule
from django.utils import timezone

def creer_donnees_test():
    """Créer des données de test pour vérifier le problème"""
    print("=== Création des données de test ===")
    
    # Créer un chauffeur de test s'il n'existe pas
    chauffeur, created = Utilisateur.objects.get_or_create(
        username='chauffeur_test',
        defaults={
            'first_name': 'Chauffeur',
            'last_name': 'Test',
            'email': 'chauffeur@test.com',
            'role': 'chauffeur',
            'is_active': True
        }
    )
    if created:
        chauffeur.set_password('password123')
        chauffeur.save()
        print(f"✅ Chauffeur test créé: {chauffeur.username}")
    else:
        print(f"ℹ️  Chauffeur test existant: {chauffeur.username}")
    
    # Créer un demandeur de test s'il n'existe pas
    demandeur, created = Utilisateur.objects.get_or_create(
        username='demandeur_test',
        defaults={
            'first_name': 'Demandeur',
            'last_name': 'Test',
            'email': 'demandeur@test.com',
            'role': 'demandeur',
            'is_active': True
        }
    )
    if created:
        demandeur.set_password('password123')
        demandeur.save()
        print(f"✅ Demandeur test créé: {demandeur.username}")
    else:
        print(f"ℹ️  Demandeur test existant: {demandeur.username}")
    
    # Créer un véhicule de test s'il n'existe pas
    vehicule, created = Vehicule.objects.get_or_create(
        immatriculation='TEST-123',
        defaults={
            'marque': 'Toyota',
            'modele': 'Corolla',
            'couleur': 'Noir',
            'numero_chassis': 'TEST123456789',
            'date_expiration_assurance': timezone.now().date() + timezone.timedelta(days=365),
            'date_expiration_controle_technique': timezone.now().date() + timezone.timedelta(days=180),
            'date_expiration_vignette': timezone.now().date() + timezone.timedelta(days=365),
            'date_expiration_stationnement': timezone.now().date() + timezone.timedelta(days=365),
        }
    )
    if created:
        print(f"✅ Véhicule test créé: {vehicule.immatriculation}")
    else:
        print(f"ℹ️  Véhicule test existant: {vehicule.immatriculation}")
    
    # Supprimer les anciennes courses de test
    old_courses = Course.objects.filter(demandeur=demandeur)
    old_count = old_courses.count()
    old_courses.delete()
    if old_count > 0:
        print(f"🗑️  {old_count} anciennes courses de test supprimées")
    
    # Créer différentes courses de test
    courses_test = [
        {
            'statut': 'en_attente',
            'chauffeur': chauffeur,
            'vehicule': vehicule,
            'description': 'Course assignée mais en attente de validation'
        },
        {
            'statut': 'validee',
            'chauffeur': chauffeur,
            'vehicule': vehicule,
            'description': 'Course validée et prête à démarrer'
        },
        {
            'statut': 'en_cours',
            'chauffeur': chauffeur,
            'vehicule': vehicule,
            'description': 'Course actuellement en cours'
        },
        {
            'statut': 'terminee',
            'chauffeur': chauffeur,
            'vehicule': vehicule,
            'description': 'Course terminée'
        },
        {
            'statut': 'en_attente',
            'chauffeur': None,
            'vehicule': None,
            'description': 'Course en attente sans assignation'
        }
    ]
    
    courses_crees = []
    for i, course_data in enumerate(courses_test, 1):
        course = Course.objects.create(
            demandeur=demandeur,
            point_embarquement=f'Point embarquement {i}',
            destination=f'Destination {i}',
            motif=f'Test course {i}: {course_data["description"]}',
            statut=course_data['statut'],
            chauffeur=course_data['chauffeur'],
            vehicule=course_data['vehicule'],
            date_souhaitee=timezone.now() + timezone.timedelta(hours=i),
            date_validation=timezone.now() if course_data['statut'] != 'en_attente' else None
        )
        courses_crees.append(course)
        print(f"✅ Course #{course.id} créée: {course.get_statut_display()} - {course_data['description']}")
    
    return courses_crees, chauffeur

def verifier_affichage_chauffeur(courses, chauffeur):
    """Vérifier que les courses s'affichent correctement pour le chauffeur"""
    print("\n=== Vérification de l'affichage pour le chauffeur ===")
    
    # Simuler la requête du dashboard chauffeur
    courses_du_chauffeur = Course.objects.select_related('demandeur', 'vehicule', 'dispatcher').filter(chauffeur=chauffeur).filter(
        Q(statut='validee') | Q(statut='en_cours') | Q(statut='terminee') | 
        (Q(statut='en_attente') & Q(chauffeur=chauffeur))
    ).order_by('-date_validation', '-date_demande')
    
    print(f"📊 Courses trouvées pour le chauffeur {chauffeur.username}: {courses_du_chauffeur.count()}")
    
    for course in courses_du_chauffeur:
        print(f"  - Course #{course.id}: {course.get_statut_display()} ({course.statut})")
        print(f"    Trajet: {course.point_embarquement} → {course.destination}")
        print(f"    Véhicule: {course.vehicule.immatriculation if course.vehicule else 'Non assigné'}")
    
    # Vérifier les statistiques
    stats = {
        'total': Course.objects.filter(chauffeur=chauffeur).count(),
        'a_effectuer': Course.objects.filter(chauffeur=chauffeur, statut='validee').count(),
        'en_attente_assignees': Course.objects.filter(chauffeur=chauffeur, statut='en_attente').count(),
        'en_cours': Course.objects.filter(chauffeur=chauffeur, statut='en_cours').count(),
        'terminees': Course.objects.filter(chauffeur=chauffeur, statut='terminee').count(),
    }
    
    print(f"\n📈 Statistiques pour {chauffeur.username}:")
    print(f"  - Total: {stats['total']}")
    print(f"  - À effectuer: {stats['a_effectuer']}")
    print(f"  - En attente (assignées): {stats['en_attente_assignees']}")
    print(f"  - En cours: {stats['en_cours']}")
    print(f"  - Terminées: {stats['terminees']}")
    
    # Vérifier le problème principal
    courses_en_attente_assignees = Course.objects.filter(chauffeur=chauffeur, statut='en_attente')
    if courses_en_attente_assignees.exists():
        print(f"\n✅ SUCCÈS: {courses_en_attente_assignees.count()} course(s) en attente assignée(s) trouvée(s)")
        for course in courses_en_attente_assignees:
            print(f"  - Course #{course.id} apparaît bien dans le dashboard")
    else:
        print(f"\n❌ PROBLÈME: Aucune course en attente assignée trouvée")
    
    return stats

from django.db.models import Q

def main():
    """Fonction principale"""
    print("🔧 Test de correction du problème des courses assignées non visibles")
    print("=" * 60)
    
    try:
        # Créer les données de test
        courses, chauffeur = creer_donnees_test()
        
        # Vérifier l'affichage
        stats = verifier_affichage_chauffeur(courses, chauffeur)
        
        print("\n" + "=" * 60)
        print("📋 RÉSUMÉ DU TEST:")
        print("=" * 60)
        
        if stats['en_attente_assignees'] > 0:
            print("✅ SUCCÈS: Les courses assignées en attente sont maintenant visibles!")
            print("🎯 Le problème a été corrigé.")
        else:
            print("❌ ÉCHEC: Les courses assignées en attente ne sont toujours pas visibles.")
            print("🔍 Vérifiez la configuration et les données de test.")
        
        print("\n🌐 Pour tester manuellement:")
        print(f"1. Connectez-vous en tant que: {chauffeur.username}")
        print("2. Accédez au dashboard chauffeur")
        print("3. Vérifiez que les courses en attente apparaissent")
        print("4. Utilisez l'outil de diagnostic: /chauffeur/diagnostic/")
        
    except Exception as e:
        print(f"❌ Erreur lors du test: {e}")
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    main()
