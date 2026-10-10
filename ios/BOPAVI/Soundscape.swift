import Foundation
import AVFoundation

/// Original bundled loops/effects; no network, purchases or third-party audio SDK.
final class Soundscape {
    private var music:AVAudioPlayer?
    private var fx:[String:AVAudioPlayer]=[:]
    var musicVolume:Float=0.80 {
        didSet {
            musicVolume=max(0,min(1,musicVolume))
            music?.volume=0.25*musicVolume
        }
    }
    var effectsVolume:Float=0.70 {
        didSet {
            effectsVolume=max(0,min(1,effectsVolume))
            for audio in fx.values{audio.volume=0.42*effectsVolume}
        }
    }
    var enabled=true {
        didSet {if !enabled {pause()} else {resume()} }
    }
    init() {
        try? AVAudioSession.sharedInstance().setCategory(.ambient,mode:.default,options:[.mixWithOthers])
        for name in ["tap","collect","level","hit","purchase","click"] {
            if let url=Bundle.main.url(forResource:name,withExtension:"wav",subdirectory:"Audio"),
               let sound=try? AVAudioPlayer(contentsOf:url) {
                sound.volume=0.42*effectsVolume;sound.prepareToPlay();fx[name]=sound
            }
        }
    }
    func startWorld(_ world:Int) {
        stop()
        guard (0..<8).contains(world),let url=Bundle.main.url(forResource:"world_\(world)",withExtension:"wav",subdirectory:"Audio") else{return}
        music=try? AVAudioPlayer(contentsOf:url)
        music?.numberOfLoops = -1;music?.volume=0.25*musicVolume;music?.prepareToPlay()
        if enabled {music?.play()}
    }
    func effect(_ name:String) {if enabled,let player=fx[name] {player.currentTime=0;player.play()} }
    func pause(){music?.pause()}
    func resume(){if enabled {music?.play()} }
    func stop(){music?.stop();music=nil}
}
