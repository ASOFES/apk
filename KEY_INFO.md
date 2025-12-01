# 🔐 Informations de la Clé de Signature

## 📋 Détails de la Clé

**Fichier:** `gestion-vehicules.keystore`
**Type:** PKCS12
**Validité:** 27 nov. 2025 → 14 avr. 2053 (10,000 jours)

### 🔑 Identifiants
- **Store Password:** `gestion123`
- **Key Alias:** `gestion-vehicules`
- **Key Password:** `gestion123`

### 📄 Certificat
- **Propriétaire:** CN=Gestion Vehicules, OU=Mobile, O=Dev, L=City, ST=State, C=FR
- **Algorithme:** SHA384withRSA
- **Clé:** RSA 2048 bits
- **Numéro de série:** ce8501308f3a1334

### 🔍 Empreintes
- **SHA-1:** `17:67:F3:DC:F7:B6:16:3D:52:6A:AF:62:51:17:69:93:B3:5D:0E:5C`
- **SHA-256:** `ED:FB:4F:45:E3:50:9C:49:D5:9C:03:7F:49:F6:DF:25:BD:2F:12:ED:01:70:9B:70:8B:B6:B4:7A:71:BD:F4:14`

---

## 📱 APK Signé avec cette Clé

**Fichier généré:** `app/build/outputs/apk/release/app-release.apk`
**Statut:** ✅ Signé et prêt pour installation

---

## 🔧 Commandes Utiles

### Vérifier la clé
```bash
keytool -list -v -keystore gestion-vehicules.keystore -storepass gestion123
```

### Vérifier la signature de l'APK
```bash
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

### Signer manuellement (si nécessaire)
```bash
jarsigner -verbose -sigalg SHA384withRSA -digestalg SHA384 -keystore gestion-vehicules.keystore -storepass gestion123 -keypass gestion123 app-release.apk gestion-vehicules
```

---

## ⚠️ Important

1. **Gardez cette clé sécurisée** - elle est unique pour cette application
2. **Sauvegardez le fichier .keystore** - il est nécessaire pour les futures mises à jour
3. **Ne perdez pas les mots de passe** - ils sont nécessaires pour signer les futures versions
4. **Utilisez toujours la même clé** pour signer les mises à jour de cette application

---

## 🔄 Pour les Futures Mises à Jour

Quand vous voudrez publier une mise à jour:
1. Utilisez le même fichier `gestion-vehicules.keystore`
2. Utilisez les mêmes mots de passe (`gestion123`)
3. Incrémentez le `versionCode` dans `build.gradle`
4. Générez le nouvel APK avec `./gradlew assembleRelease`

Cela garantira que les utilisateurs pourront installer la mise à jour sans avoir à désinstaller l'ancienne version.
