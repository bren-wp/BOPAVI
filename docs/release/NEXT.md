## v1.0.1 — Native Android-only migration and release safeguards (build 131)
- Remove the obsolete iOS project, simulator build, Swift tests, iOS resources, and iOS-specific release packaging; preserve original BOPAVI graphics and Android audio.
- Harden offline Android asset production, Android-only CI contracts, and the tested release process without changing saved progress keys or the com.brendigo.bopavi application identity.
- Preserve deterministic flight physics and existing circle/rounded-pillar collision model pending further measured device-level tuning; run Android gameplay, emulator, and collision regression gates before publishing.
- Keep all existing public GitHub v1+ releases unchanged and prevent any iOS artifacts from future Android-only releases.

- Pause music and abandon audio focus on interruption, resume only with valid audio focus, and release SoundPool/MediaPlayer safely; no new Android permissions or audio dependencies.
