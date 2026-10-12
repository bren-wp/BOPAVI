import UIKit
import UniformTypeIdentifiers

/// Premium, self-contained control. The gradient resizes without a new image allocation.
private final class BopaviActionButton: UIButton {
    private let gradient = CAGradientLayer()
    init(primary:Bool) {
        super.init(frame:.zero)
        gradient.colors = primary
            ? [UIColor(red:1.00,green:0.88,blue:0.32,alpha:1).cgColor,
               UIColor(red:1.00,green:0.67,blue:0.09,alpha:1).cgColor,
               UIColor(red:1.00,green:0.46,blue:0.00,alpha:1).cgColor]
            : [UIColor(red:0.14,green:0.79,blue:1.00,alpha:1).cgColor,
               UIColor(red:0.03,green:0.49,blue:0.93,alpha:1).cgColor,
               UIColor(red:0.02,green:0.30,blue:0.71,alpha:1).cgColor]
        gradient.startPoint=CGPoint(x:0.5,y:0);gradient.endPoint=CGPoint(x:0.5,y:1)
        layer.insertSublayer(gradient,at:0)
        layer.cornerRadius=29
        layer.borderWidth=primary ? 2 : 1
        layer.borderColor=(primary
            ? UIColor(red:1,green:0.94,blue:0.60,alpha:1)
            : UIColor(red:0.47,green:0.91,blue:1,alpha:1)).cgColor
        layer.shadowColor=(primary
            ? UIColor(red:0.55,green:0.20,blue:0.0,alpha:1)
            : UIColor(red:0.0,green:0.48,blue:0.95,alpha:1)).cgColor
        layer.shadowOpacity=0.40
        layer.shadowRadius=9
        layer.shadowOffset=CGSize(width:0,height:4)
        setTitleColor(.white,for:.normal)
        titleLabel?.font=UIFont.systemFont(ofSize:18,weight:.heavy)
        titleLabel?.adjustsFontSizeToFitWidth=true
        titleLabel?.minimumScaleFactor=0.72
        titleLabel?.numberOfLines=1
        contentEdgeInsets=UIEdgeInsets(top:8,left:12,bottom:8,right:12)
        accessibilityTraits.insert(.button)
    }
    required init?(coder:NSCoder){fatalError("Use programmatic initialization")}
    override func layoutSubviews(){
        super.layoutSubviews()
        gradient.frame=bounds
        gradient.cornerRadius=29
        layer.shadowPath=UIBezierPath(roundedRect:bounds,cornerRadius:29).cgPath
    }
}

/// Gradient-backed Settings sheet; reflows with Dynamic Type and safe areas.
private final class BopaviSettingsPanel: UIStackView {
    private let fill=CAGradientLayer()
    override init(frame:CGRect) {
        super.init(frame:frame)
        axis = .vertical
        alignment = .fill
        spacing = 10
        isLayoutMarginsRelativeArrangement = true
        layoutMargins = UIEdgeInsets(top:14,left:13,bottom:18,right:13)
        fill.colors = [
            UIColor(red:0.024,green:0.196,blue:0.431,alpha:0.97).cgColor,
            UIColor(red:0.016,green:0.086,blue:0.192,alpha:0.99).cgColor
        ]
        fill.startPoint=CGPoint(x:0.5,y:0)
        fill.endPoint=CGPoint(x:0.5,y:1)
        fill.cornerRadius=27
        layer.insertSublayer(fill,at:0)
        layer.cornerRadius=27
        layer.borderWidth=2
        layer.borderColor=UIColor(red:0.259,green:0.812,blue:1,alpha:1).cgColor
        layer.shadowColor=UIColor(red:0,green:0.3,blue:0.8,alpha:1).cgColor
        layer.shadowOpacity=0.46
        layer.shadowRadius=12
        layer.shadowOffset=CGSize(width:0,height:6)
    }
    required init(coder:NSCoder){fatalError("Use programmatic initialization")}
    override func layoutSubviews(){
        super.layoutSubviews()
        fill.frame=bounds
        layer.shadowPath=UIBezierPath(roundedRect:bounds,cornerRadius:27).cgPath
    }
}

/** Native UIKit menus and Core Graphics gameplay, no WKWebView. */
final class GameController: UIViewController, UIDocumentPickerDelegate {
    private enum MenuOrigin { case home, settings, profile, shop }
    private let progress=ProgressStore()
    private let sound=Soundscape()
    private let collectHaptic=UISelectionFeedbackGenerator()
    private let shieldHaptic=UIImpactFeedbackGenerator(style:.medium)
    private var canvas:GameCanvas?
    private var gameWorld=0
    private var gameNumber=1
    private var backgroundGradient:CAGradientLayer?
    private let worldAccents:[UIColor] = [
        UIColor(red:0.23,green:0.83,blue:0.56,alpha:1), UIColor(red:0.50,green:0.92,blue:1,alpha:1),
        UIColor(red:1,green:0.51,blue:0.39,alpha:1), UIColor(red:0.72,green:0.60,blue:0.98,alpha:1),
        UIColor(red:0.39,green:0.91,blue:0.88,alpha:1), UIColor(red:1,green:0.85,blue:0.57,alpha:1),
        UIColor(red:0.70,green:0.93,blue:0.45,alpha:1), UIColor(red:0.74,green:0.69,blue:1,alpha:1)
    ]
    override var preferredStatusBarStyle:UIStatusBarStyle {.lightContent}
    // Full-bleed menus retain UIKit safe-area protection for device cutouts.
    override var prefersStatusBarHidden:Bool { true }
    override var prefersHomeIndicatorAutoHidden:Bool { canvas != nil }
    override func viewDidLoad(){
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self,selector:#selector(pauseForInterruption(_:)),
            name:UIApplication.willResignActiveNotification,object:nil)
        NotificationCenter.default.addObserver(self,selector:#selector(resumeIdlePreview(_:)),
            name:UIApplication.didBecomeActiveNotification,object:nil)
        sound.enabled=progress.soundEnabled
        sound.musicVolume=Float(progress.musicVolume)/100
        sound.effectsVolume=Float(progress.effectsVolume)/100
        showHome()
    }
    deinit { NotificationCenter.default.removeObserver(self) }
    @objc private func pauseForInterruption(_ note:Notification) {
        // Keep an active flight exactly where the user left it on call, lock or app switch.
        // Never auto-resume physics or audio in the background.
        guard let activeCanvas=canvas else {return}
        if activeCanvas.game.active {activeCanvas.paused=true}
        sound.pause()
    }
    @objc private func resumeIdlePreview(_ note:Notification) {
        // Idle preview has no pause button; restore its soundtrack on return.
        if let activeCanvas=canvas, !activeCanvas.game.active && !activeCanvas.paused {
            sound.resume()
        }
    }
    private func clear() {
        canvas?.stop();canvas=nil;sound.stop()
        setNeedsStatusBarAppearanceUpdate()
        backgroundGradient=nil
        view.subviews.forEach{$0.removeFromSuperview()}
        view.layer.sublayers?.forEach{$0.removeFromSuperlayer()}
        view.backgroundColor=UIColor(red:0.03,green:0.10,blue:0.24,alpha:1)
    }
    // Menus share the illustrated backgrounds and gold / cyan / navy palette.
    // Gameplay and persistence remain independent of these presentation views.
    private func menu(_ title:String,_ subtitle:String)->UIStackView {
        clear()
        if title != "BOPAVI" {
            let world=gameWorld >= 0 && gameWorld < 8 ? gameWorld : 0
            let backdrop=UIImageView(image:UIImage(named:"World\(world)"))
            backdrop.translatesAutoresizingMaskIntoConstraints=false
            backdrop.contentMode = .scaleAspectFill
            backdrop.clipsToBounds=true
            backdrop.isAccessibilityElement=false
            view.addSubview(backdrop)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
            let shade=UIView()
            shade.backgroundColor=UIColor(red:0.012,green:0.08,blue:0.19,alpha:0.75)
            shade.translatesAutoresizingMaskIntoConstraints=false
            view.addSubview(shade)
            NSLayoutConstraint.activate([
                shade.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                shade.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                shade.topAnchor.constraint(equalTo:view.topAnchor),
                shade.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
        }
        let scroll=UIScrollView()
        scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.alwaysBounceVertical=true
        scroll.showsVerticalScrollIndicator=false
        scroll.keyboardDismissMode = .interactive
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        let stack=UIStackView()
        stack.axis = .vertical;stack.alignment = .fill;stack.spacing=12
        stack.translatesAutoresizingMaskIntoConstraints=false
        stack.layoutMargins=UIEdgeInsets(top:18,left:18,bottom:28,right:18)
        stack.isLayoutMarginsRelativeArrangement=true
        scroll.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            stack.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            stack.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            stack.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor)])
        if title != "BOPAVI" {
            let logo=UIImageView(image:UIImage(named:"Logo"))
            logo.contentMode = .scaleAspectFit
            logo.accessibilityElementsHidden=true
            logo.heightAnchor.constraint(equalToConstant:94).isActive=true
            if title == "POSTAVKE" {
                // Same selected-pilot header arrangement as Android, drawn from
                // shipped standalone portrait art rather than a reference PNG.
                let pilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
                pilot.contentMode = .scaleAspectFit
                pilot.isAccessibilityElement=false
                pilot.widthAnchor.constraint(equalToConstant:117).isActive=true
                pilot.heightAnchor.constraint(equalToConstant:117).isActive=true
                let hero=UIStackView(arrangedSubviews:[logo,pilot])
                hero.axis = .horizontal
                hero.alignment = .center
                hero.spacing = 3
                hero.heightAnchor.constraint(equalToConstant:118).isActive=true
                stack.addArrangedSubview(hero)
            } else {
                stack.addArrangedSubview(logo)
            }
            let headline=label(title,31,.white,stack)
            headline.backgroundColor=UIColor(red:0.015,green:0.25,blue:0.56,alpha:0.96)
            headline.layer.cornerRadius=23
            headline.layer.borderWidth=2
            headline.layer.borderColor=UIColor(red:0.28,green:0.83,blue:1,alpha:1).cgColor
            headline.clipsToBounds=true
            headline.heightAnchor.constraint(greaterThanOrEqualToConstant:58).isActive=true
            if !subtitle.isEmpty {label(subtitle,15,UIColor(red:0.79,green:0.93,blue:1,alpha:1),stack)}
        }
        return stack
    }
    override func viewDidLayoutSubviews(){super.viewDidLayoutSubviews();backgroundGradient?.frame=view.bounds}
    @discardableResult private func label(_ text:String,_ size:CGFloat,_ color:UIColor,_ into:UIStackView)->UILabel {
        let l=UILabel()
        l.numberOfLines=0;l.textAlignment = .center;l.text=text
        l.font=UIFont.systemFont(ofSize:size,weight:size>=26 ? .black : .semibold)
        l.adjustsFontForContentSizeCategory=true
        l.textColor=color
        l.setContentCompressionResistancePriority(.required,for:.vertical);into.addArrangedSubview(l)
        return l
    }
    private func sectionHeading(_ title:String,in stack:UIStackView) {
        let heading=label("   "+title,15,.white,stack)
        heading.textAlignment = .left
        heading.font=UIFont.systemFont(ofSize:15,weight:.heavy)
        heading.backgroundColor=UIColor(red:0.03,green:0.16,blue:0.36,alpha:0.97)
        heading.layer.cornerRadius=20
        heading.layer.borderWidth=1
        heading.layer.borderColor=UIColor(red:0.25,green:0.72,blue:1,alpha:0.65).cgColor
        heading.clipsToBounds=true
        heading.heightAnchor.constraint(greaterThanOrEqualToConstant:44).isActive=true
    }
    private func button(_ title:String,in stack:UIStackView,primary:Bool=true,action:@escaping()->Void){
        let b=BopaviActionButton(primary:primary)
        b.setTitle(title,for:.normal)
        b.accessibilityLabel=title
        b.heightAnchor.constraint(greaterThanOrEqualToConstant:primary ? 68 : 58).isActive=true
        b.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        stack.addArrangedSubview(b)
    }
    private func worldTile(_ world:Int,in stack:UIStackView,action:@escaping()->Void){
        // Reference-like compact 2×4 gallery: all art, headings and earned stars live inside each card.
        // All eight worlds remain immediately playable; the highlight indicates
        // only the user's currently selected world, never a paid/locked world.
        let selected=progress.chosenWorld()==world
        let tile=BopaviActionButton(primary:false)
        tile.setTitle("",for:.normal)
        tile.layer.borderWidth=selected ? 3 : 1
        tile.layer.borderColor=worldAccents[world].withAlphaComponent(selected ? 1 : 0.65).cgColor
        // Exactly the same genuine values as Android's saved world cards.
        tile.accessibilityLabel="\(BopaviCore.names[world]), otključano, " +
            "level \(progress.streamFrontier(world)), prikupljeno \(progress.collectibles(world)), " +
            "rekord \(progress.best(world)), zvjezdice \(progress.worldStars(world)) od 30" +
            (selected ? ", odabrano" : "") +
            (world % 2 == 1 ? ", upravljanje povlačenjem" : ", upravljanje dodirom")
        tile.heightAnchor.constraint(equalToConstant:144).isActive=true
        tile.accessibilityHint="Otvori levele svijeta"
        if selected {tile.accessibilityTraits.insert(.selected)}
        if let illustration=UIImage(named:"World\(world)") {
            let preview=UIImageView(image:illustration)
            preview.translatesAutoresizingMaskIntoConstraints=false
            preview.contentMode = .scaleAspectFill
            preview.clipsToBounds=true
            preview.layer.cornerRadius=15
            preview.isUserInteractionEnabled=false
            preview.accessibilityElementsHidden=true
            tile.addSubview(preview)
            NSLayoutConstraint.activate([
                preview.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:6),
                preview.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-6),
                preview.topAnchor.constraint(equalTo:tile.topAnchor,constant:6),
                preview.heightAnchor.constraint(equalToConstant:132)
            ])
        }
        let footerShade=UIView()
        footerShade.translatesAutoresizingMaskIntoConstraints=false
        footerShade.backgroundColor=UIColor(red:0.01,green:0.06,blue:0.16,alpha:0.76)
        footerShade.isUserInteractionEnabled=false
        footerShade.accessibilityElementsHidden=true
        tile.addSubview(footerShade)
        NSLayoutConstraint.activate([
            footerShade.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:6),
            footerShade.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-6),
            footerShade.topAnchor.constraint(equalTo:tile.topAnchor,constant:83),
            footerShade.heightAnchor.constraint(equalToConstant:55)
        ])
        // Decorative (non-interactive) gold world number and blue forward glyph
        // sit on the real illustration, with the whole card as the tap target.
        let number=UILabel()
        number.text="♛  \(world+1)"
        number.textColor = .white;number.textAlignment = .center
        number.font=UIFont.systemFont(ofSize:15,weight:.heavy)
        number.backgroundColor=selected
            ? UIColor(red:0.78,green:0.48,blue:0.04,alpha:0.95)
            : UIColor(red:0.03,green:0.39,blue:0.83,alpha:0.95)
        number.layer.cornerRadius=18;number.clipsToBounds=true
        number.layer.borderWidth=2
        number.layer.borderColor=selected
            ? UIColor(red:1,green:0.83,blue:0.33,alpha:1).cgColor
            : UIColor(red:0.35,green:0.85,blue:1,alpha:1).cgColor
        number.translatesAutoresizingMaskIntoConstraints=false
        number.isUserInteractionEnabled=false
        number.accessibilityElementsHidden=true
        tile.addSubview(number)
        // Same control mode as the real simulation, not a decorative button.
        let mode=UILabel()
        mode.text=world % 2 == 1 ? "↕ POVUCI" : "● TAP"
        mode.font=UIFont.systemFont(ofSize:10,weight:.heavy)
        mode.textAlignment = .center;mode.textColor = .white
        mode.backgroundColor=world % 2 == 1
            ? UIColor(red:0.37,green:0.22,blue:0.70,alpha:0.97)
            : UIColor(red:0.04,green:0.39,blue:0.70,alpha:0.97)
        mode.layer.cornerRadius=14;mode.clipsToBounds=true
        mode.layer.borderWidth=1
        mode.layer.borderColor=UIColor(red:0.35,green:0.85,blue:1,alpha:1).cgColor
        mode.isUserInteractionEnabled=false;mode.accessibilityElementsHidden=true
        mode.translatesAutoresizingMaskIntoConstraints=false;tile.addSubview(mode)
        NSLayoutConstraint.activate([
            mode.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:12),
            mode.topAnchor.constraint(equalTo:tile.topAnchor,constant:54),
            mode.widthAnchor.constraint(equalToConstant:85),
            mode.heightAnchor.constraint(equalToConstant:29)
        ])
        let arrow=UILabel()
        arrow.text="›";arrow.font=UIFont.systemFont(ofSize:30,weight:.heavy)
        arrow.textColor = .white;arrow.textAlignment = .center
        arrow.backgroundColor=UIColor(red:0.02,green:0.48,blue:0.90,alpha:0.95)
        arrow.layer.cornerRadius=18;arrow.clipsToBounds=true
        arrow.layer.borderWidth=1
        arrow.layer.borderColor=UIColor(red:0.35,green:0.90,blue:1,alpha:1).cgColor
        arrow.translatesAutoresizingMaskIntoConstraints=false
        arrow.isUserInteractionEnabled=false
        arrow.accessibilityElementsHidden=true
        tile.addSubview(arrow)
        NSLayoutConstraint.activate([
            number.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:12),
            number.topAnchor.constraint(equalTo:tile.topAnchor,constant:12),
            number.widthAnchor.constraint(equalToConstant:64),
            number.heightAnchor.constraint(equalToConstant:36),
            arrow.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-12),
            arrow.topAnchor.constraint(equalTo:tile.topAnchor,constant:98),
            arrow.widthAnchor.constraint(equalToConstant:36),
            arrow.heightAnchor.constraint(equalToConstant:36)
        ])
        let headline=UILabel()
        headline.translatesAutoresizingMaskIntoConstraints=false
        headline.text=BopaviCore.names[world].uppercased()
        headline.textColor = .white
        headline.font=UIFont.systemFont(ofSize:15,weight:.black)
        headline.adjustsFontSizeToFitWidth=true
        headline.minimumScaleFactor=0.72
        headline.textAlignment = .left
        headline.shadowColor=UIColor.black.withAlphaComponent(0.7)
        headline.shadowOffset=CGSize(width:0,height:2)
        headline.isUserInteractionEnabled=false
        headline.accessibilityElementsHidden=true
        tile.addSubview(headline)
         NSLayoutConstraint.activate([
            headline.topAnchor.constraint(equalTo:tile.topAnchor,constant:85),
            headline.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:14),
            headline.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-12),
            headline.heightAnchor.constraint(equalToConstant:29)
        ])
        let collection=UILabel()
        collection.translatesAutoresizingMaskIntoConstraints=false
        collection.text="★ \(progress.worldStars(world))/30   ·   ♛ \(progress.best(world))"
        collection.textColor=UIColor(red:1,green:0.80,blue:0.34,alpha:1)
        collection.font=UIFont.monospacedDigitSystemFont(ofSize:12,weight:.heavy)
        collection.adjustsFontSizeToFitWidth=true
        collection.minimumScaleFactor=0.78
        collection.textAlignment = .left
        collection.isUserInteractionEnabled=false
        collection.accessibilityElementsHidden=true
        tile.addSubview(collection)
        NSLayoutConstraint.activate([
            collection.topAnchor.constraint(equalTo:tile.topAnchor,constant:114),
            collection.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:14),
            collection.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-52),
            collection.heightAnchor.constraint(equalToConstant:20)
        ])
        tile.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        stack.addArrangedSubview(tile)
    }
    private func showHome(){
        let stack=menu("BOPAVI","")
        view.backgroundColor=UIColor(red:0.04,green:0.45,blue:0.79,alpha:1)
        if let artwork=UIImage(named:"HomeArt") {
            let backdrop=UIImageView(image:artwork)
            backdrop.translatesAutoresizingMaskIntoConstraints=false
            backdrop.contentMode = .scaleAspectFill
            backdrop.clipsToBounds=true
            backdrop.isUserInteractionEnabled=false
            backdrop.accessibilityElementsHidden=true
            view.insertSubview(backdrop,at:0)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
        }
        // The art already includes the mascot and game title; only live,
        // clickable controls and player-dependent values sit on top.
        stack.heightAnchor.constraint(greaterThanOrEqualTo:view.safeAreaLayoutGuide.heightAnchor).isActive=true
        // Real home navigation: current pilot, earned results, coins and
        // settings. Each separately accessible element has a working action.
        let walletRow=UIStackView()
        walletRow.axis = .horizontal
        walletRow.alignment = .center
        walletRow.distribution = .fill
        walletRow.spacing=5
        let avatar=UIButton(type:.custom)
        avatar.setImage(UIImage(named:"BopiPortrait\(progress.skinIndex())"),for:.normal)
        avatar.imageView?.contentMode = .scaleAspectFit
        avatar.contentVerticalAlignment = .fill
        avatar.contentHorizontalAlignment = .fill
        avatar.backgroundColor=UIColor(red:0.02,green:0.28,blue:0.59,alpha:0.96)
        avatar.layer.cornerRadius=24
        avatar.layer.borderWidth=2
        avatar.layer.borderColor=UIColor(red:1,green:0.78,blue:0.22,alpha:1).cgColor
        avatar.clipsToBounds=true
        avatar.accessibilityLabel="PROFIL · \(progress.skinNames[progress.skinIndex()])"
        avatar.widthAnchor.constraint(equalToConstant:48).isActive=true
        avatar.heightAnchor.constraint(equalToConstant:48).isActive=true
        avatar.addAction(UIAction{_ in self.sound.effect("click");self.showProfile()},for:.touchUpInside)
        walletRow.addArrangedSubview(avatar)
        func headerChip(_ title:String,_ description:String,_ color:UIColor,
                        _ action:@escaping()->Void)->UIButton {
            let chip=UIButton(type:.system)
            chip.setTitle(title,for:.normal)
            chip.setTitleColor(color,for:.normal)
            chip.titleLabel?.font=UIFont.systemFont(ofSize:15,weight:.heavy)
            chip.titleLabel?.adjustsFontSizeToFitWidth=true
            chip.titleLabel?.minimumScaleFactor=0.65
            chip.backgroundColor=UIColor(red:0.03,green:0.14,blue:0.35,alpha:0.93)
            chip.layer.cornerRadius=20
            chip.layer.borderWidth=1
            chip.layer.borderColor=UIColor(red:0.40,green:0.84,blue:1,alpha:0.9).cgColor
            chip.accessibilityLabel=description
            chip.heightAnchor.constraint(equalToConstant:48).isActive=true
            chip.setContentHuggingPriority(.defaultLow,for:.horizontal)
            chip.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
            chip.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
            return chip
        }
        let best=headerChip("🏆  \(progress.bestPoints())",
            "ZADACI I POSTIGNUĆA · najbolji rezultat \(progress.bestPoints())",
            .white){self.showAchievements(.home)}
        walletRow.addArrangedSubview(best)
        let coins=headerChip("♛  \(progress.coins())",
            "TRGOVINA · \(progress.coins()) osvojenih kovanica",
            UIColor(red:1,green:0.83,blue:0.37,alpha:1)){self.showPerks(.home)}
        walletRow.addArrangedSubview(coins)
        best.widthAnchor.constraint(equalTo:coins.widthAnchor).isActive=true
        let gear=UIButton(type:.system)
        gear.setTitle("⚙",for:.normal)
        gear.setTitleColor(.white,for:.normal)
        gear.titleLabel?.font=UIFont.systemFont(ofSize:26,weight:.bold)
        gear.backgroundColor=UIColor(red:0.03,green:0.29,blue:0.61,alpha:0.96)
        gear.layer.cornerRadius=17
        gear.layer.borderWidth=2
        gear.layer.borderColor=UIColor(red:0.27,green:0.84,blue:1,alpha:1).cgColor
        gear.accessibilityLabel="POSTAVKE"
        gear.widthAnchor.constraint(equalToConstant:48).isActive=true
        gear.heightAnchor.constraint(equalToConstant:48).isActive=true
        gear.addAction(UIAction{_ in self.sound.effect("click");self.showSettings()},for:.touchUpInside)
        walletRow.addArrangedSubview(gear)
        stack.addArrangedSubview(walletRow)
        let spacer=UIView()
        spacer.setContentHuggingPriority(.defaultLow,for:.vertical)
        stack.addArrangedSubview(spacer)
        button("▶  IGRAJ",in:stack){
            let world=self.progress.chosenWorld()
            self.showPilotPicker(world,self.progress.streamFrontier(world))
        }
        button("🌍  SVJETOVI",in:stack,primary:false){self.showWorlds()}
        button("⚙  POSTAVKE",in:stack,primary:false){self.showSettings()}
        let navigation=UIStackView()
        navigation.axis = .horizontal
        navigation.distribution = .fillEqually
        navigation.spacing=5
        navigation.layoutMargins=UIEdgeInsets(top:8,left:7,bottom:8,right:7)
        navigation.isLayoutMarginsRelativeArrangement=true
        navigation.backgroundColor=UIColor(red:0.012,green:0.13,blue:0.32,alpha:0.96)
        navigation.layer.cornerRadius=22
        navigation.layer.borderWidth=2
        navigation.layer.borderColor=UIColor(red:0.15,green:0.72,blue:1,alpha:1).cgColor
        let tabs:[(String,String,()->Void)]=[
            ("NavProfile","PROFIL",{self.showProfile()}),
            ("NavTasks","ZADACI",{self.showAchievements(.home)}),
            ("NavCollection","KOLEKCIJA",{self.showSkins(.home)}),
            ("NavShop","TRGOVINA",{self.showPerks(.home)})
        ]
        for (icon,title,handler) in tabs {
            let tab=UIButton(type:.system)
            tab.setTitle("",for:.normal)
            tab.accessibilityLabel=title
            tab.backgroundColor=UIColor(red:0.04,green:0.31,blue:0.63,alpha:0.70)
            tab.layer.cornerRadius=13
            tab.heightAnchor.constraint(equalToConstant:62).isActive=true
            let glyph=UIImageView(image:UIImage(named:icon))
            glyph.contentMode = .scaleAspectFit
            glyph.isAccessibilityElement=false
            let caption=UILabel()
            caption.text=title;caption.textAlignment = .center
            caption.textColor = .white
            caption.font=UIFont.systemFont(ofSize:11,weight:.bold)
            caption.adjustsFontSizeToFitWidth=true
            caption.minimumScaleFactor=0.77
            let content=UIStackView(arrangedSubviews:[glyph,caption])
            content.axis = .vertical;content.spacing=0
            content.translatesAutoresizingMaskIntoConstraints=false
            content.isUserInteractionEnabled=false
            tab.addSubview(content)
            NSLayoutConstraint.activate([
                content.leadingAnchor.constraint(equalTo:tab.leadingAnchor,constant:2),
                content.trailingAnchor.constraint(equalTo:tab.trailingAnchor,constant:-2),
                content.centerYAnchor.constraint(equalTo:tab.centerYAnchor),
                glyph.heightAnchor.constraint(equalToConstant:30),
                caption.heightAnchor.constraint(equalToConstant:20)
            ])
            tab.addAction(UIAction{_ in self.sound.effect("click");handler()},for:.touchUpInside)
            navigation.addArrangedSubview(tab)
        }
        stack.addArrangedSubview(navigation)
    }

    private func showWorlds(){
        let stack=menu("SVJETOVI","Osam različitih avantura")
        for line in 0..<4 {
            let row=UIStackView()
            row.axis = .horizontal;row.alignment = .fill
            row.distribution = .fillEqually;row.spacing=10
            stack.addArrangedSubview(row)
            for col in 0..<2 {
                let world=line*2+col
                worldTile(world,in:row){self.showLevels(world,page:1)}
            }
        }
        button("‹  Natrag",in:stack,primary:false){self.showHome()}
    }
    private func showLevels(_ world:Int,page:Int){
        gameWorld=world;progress.chooseWorld(world)
        let s=menu("LEVELI","Odaberite level · \(BopaviCore.names[world]) · " +
            (world % 2 == 1 ? "POVUCI GORE / DOLJE" : "TAP ZA ZAMAH"))
        let maxNumber=min(BopaviCore.levelsPerWorld,progress.frontier(world))
        let safePage=max(1,min(page,BopaviCore.levelsPerWorld))
        let start=((safePage-1)/20)*20+1
        let zone=BopaviCore.create(world,start).zone
        let pageNumber=(start-1)/20+1
        let pageCount=(BopaviCore.levelsPerWorld-1)/20+1
        label("ZONA \(zone) · LEVELI \(start)–\(min(start+19,BopaviCore.levelsPerWorld))",16,.white,s)
        let pageCaption="STRANICA \(pageNumber) / \(pageCount)" +
            (start>maxNumber ? " · ZAKLJUČANI LEVELI" : " · ★ \(progress.worldStars(world))/30")
        label(pageCaption,14,UIColor(red:0.87,green:0.93,blue:1,alpha:1),s)
        label("● NORMALNI · ⚡ IZAZOVNI · ✦ BONUS · ♛ ELITNI",13,.white,s)
        for rowNumber in 0..<5 {
            let row=UIStackView()
            row.axis = .horizontal;row.spacing=6;row.distribution = .fillEqually
            for column in 0..<4 {
                let n=start+rowNumber*4+column
                if n>BopaviCore.levelsPerWorld {break}
                let kind=BopaviCore.create(world,n).type
                levelTile(world,n,frontier:maxNumber,kind:kind,in:row)
            }
            if !row.arrangedSubviews.isEmpty{s.addArrangedSubview(row)}
        }
        let pager=UIStackView()
        pager.axis = .horizontal
        pager.distribution = .fillEqually
        pager.spacing=8
        if start>1 {
            button("← PRETHODNIH 20",in:pager,primary:false){
                self.showLevels(world,page:start-20)
            }
        }
        if start+20<=BopaviCore.levelsPerWorld {
            button("SLJEDEĆIH 20 →",in:pager){
                self.showLevels(world,page:start+20)
            }
        }
        if !pager.arrangedSubviews.isEmpty{s.addArrangedSubview(pager)}
        sectionHeading(start<=maxNumber ? "NASTAVI ILI ODABERI LEVEL"
            : "PREGLED ZAKLJUČANIH LEVELA",in:s)
        label("Otključano do levela \(maxNumber)",16,.white,s)
        // Locked preview pages have no flight CTA, only honest navigation.
        if start<=maxNumber {
        button("▶  NASTAVI LET",in:s){
            self.showPilotPicker(world,self.progress.streamFrontier(world))
        }
        let field=UITextField()
        field.keyboardType = .numberPad
        field.text=String(max(start,min(maxNumber,min(start+19,BopaviCore.levelsPerWorld))))
        field.placeholder="Broj otključanog levela"
        field.textAlignment = .center
        field.textColor = .white
        field.backgroundColor=UIColor(red:0.025,green:0.15,blue:0.34,alpha:1)
        field.layer.cornerRadius=17
        field.layer.borderWidth=1
        field.layer.borderColor=UIColor(red:0.26,green:0.76,blue:1,alpha:1).cgColor
        field.heightAnchor.constraint(equalToConstant:52).isActive=true
        s.addArrangedSubview(field)
        button("▶  POKRENI ODABRANI LEVEL",in:s,primary:false){
            field.resignFirstResponder()
            guard let n=Int(field.text ?? ""), (1...maxNumber).contains(n) else {
                self.alert("Nedostupan level","Odaberi otključan level iz raspona 1–\(maxNumber).")
                return
            }
            self.showPilotPicker(world,n)
        }
        } else {
            label("Ovi leveli su zaključani. Dovrši prethodne za otključavanje.",14,.white,s)
            button("↶ VRATI NA OTKLJUČANE LEVELE",in:s,primary:false){
                self.showLevels(world,page:maxNumber)
            }
        }
        button("‹  Svjetovi",in:s,primary:false){self.showWorlds()}
    }
    /// Every image is a reusable, real world illustration. Completion is read
    /// exclusively from saved frontier; no invented three-star achievements.
    private func levelTile(_ world:Int,_ n:Int,frontier:Int,kind:Int,in row:UIStackView){
        let unlocked=n<=frontier
        let completed=n<frontier
        let selected=n==frontier
        let cell=BopaviActionButton(primary:false)
        cell.setTitle("",for:.normal)
        cell.accessibilityLabel="Level \(n), \(LevelKind.name(kind)), \(!unlocked ? "zaključan" : (completed ? "dovršen" : "otključan")), " +
            "zvjezdice \(progress.bestLevelStars(world,n)) od 3"
        cell.accessibilityHint=unlocked ? "Pokreni level" : "Prvo dovrši prethodni level"
        cell.layer.cornerRadius=16
        cell.layer.borderWidth=selected ? 3 : 1
        cell.layer.borderColor=(selected
            ? UIColor(red:1,green:0.78,blue:0.30,alpha:1)
            : UIColor(red:0.43,green:0.84,blue:1,alpha:0.9)).cgColor
        cell.clipsToBounds=true
        cell.heightAnchor.constraint(equalToConstant:86).isActive=true
        let picture=UIImageView(image:UIImage(named:"World\(world)"))
        picture.translatesAutoresizingMaskIntoConstraints=false
        picture.contentMode = .scaleAspectFill
        picture.clipsToBounds=true
        picture.alpha=unlocked ? 0.96 : 0.30
        picture.isUserInteractionEnabled=false
        picture.accessibilityElementsHidden=true
        cell.addSubview(picture)
        let shade=UIView()
        shade.translatesAutoresizingMaskIntoConstraints=false
        shade.isUserInteractionEnabled=false
        shade.backgroundColor=UIColor(red:0.01,green:0.10,blue:0.28,alpha:unlocked ? 0.28 : 0.75)
        cell.addSubview(shade)
        let number=UILabel()
        number.text=String(n)
        number.textAlignment = .center
        number.font=UIFont.monospacedDigitSystemFont(ofSize:22,weight:.black)
        number.textColor=unlocked ? .white : UIColor(red:0.72,green:0.79,blue:0.87,alpha:1)
        number.layer.shadowOpacity=0.7;number.layer.shadowRadius=2
        number.translatesAutoresizingMaskIntoConstraints=false
        number.accessibilityElementsHidden=true
        cell.addSubview(number)
        let icon=UILabel()
        icon.text = !unlocked ? "🔒" : (completed ? "✓" : LevelKind.icon(kind))
        icon.textAlignment = .center
        icon.textColor=selected ? UIColor(red:1,green:0.80,blue:0.28,alpha:1) : .white
        icon.font=UIFont.systemFont(ofSize:17,weight:.bold)
        icon.translatesAutoresizingMaskIntoConstraints=false
        icon.accessibilityElementsHidden=true
        cell.addSubview(icon)
        NSLayoutConstraint.activate([
            picture.leadingAnchor.constraint(equalTo:cell.leadingAnchor),
            picture.trailingAnchor.constraint(equalTo:cell.trailingAnchor),
            picture.topAnchor.constraint(equalTo:cell.topAnchor),
            picture.bottomAnchor.constraint(equalTo:cell.bottomAnchor),
            shade.leadingAnchor.constraint(equalTo:cell.leadingAnchor),
            shade.trailingAnchor.constraint(equalTo:cell.trailingAnchor),
            shade.topAnchor.constraint(equalTo:cell.topAnchor),
            shade.bottomAnchor.constraint(equalTo:cell.bottomAnchor),
            number.topAnchor.constraint(equalTo:cell.topAnchor,constant:5),
            number.leadingAnchor.constraint(equalTo:cell.leadingAnchor),
            number.trailingAnchor.constraint(equalTo:cell.trailingAnchor),
            number.heightAnchor.constraint(equalToConstant:32),
            icon.topAnchor.constraint(equalTo:cell.topAnchor,constant:unlocked ? 0 : 22),
            icon.trailingAnchor.constraint(equalTo:cell.trailingAnchor,constant:unlocked ? -1 : 0),
            icon.widthAnchor.constraint(equalToConstant:unlocked ? 31 : 86),
            icon.heightAnchor.constraint(equalToConstant:32)
        ])
        let earned=progress.bestLevelStars(world,n)
        let rating=UILabel()
        rating.text=String(repeating:"★",count:earned)+String(repeating:"☆",count:3-earned)
        rating.textAlignment = .center
        rating.textColor=earned>0 ? UIColor(red:1,green:0.80,blue:0.26,alpha:1)
                                 : UIColor(red:0.67,green:0.78,blue:0.94,alpha:1)
        rating.font=UIFont.systemFont(ofSize:16,weight:.heavy)
        rating.translatesAutoresizingMaskIntoConstraints=false
        rating.accessibilityElementsHidden=true
        cell.addSubview(rating)
        NSLayoutConstraint.activate([
            rating.leadingAnchor.constraint(equalTo:cell.leadingAnchor),
            rating.trailingAnchor.constraint(equalTo:cell.trailingAnchor),
            rating.bottomAnchor.constraint(equalTo:cell.bottomAnchor,constant:-1),
            rating.heightAnchor.constraint(equalToConstant:24)
        ])
        cell.addAction(UIAction{_ in
            self.sound.effect("click")
            if unlocked {self.showPilotPicker(world,n)}
            else {self.alert("Zaključano","Prvo dovrši prethodni level.")}
        },for:.touchUpInside)
        row.addArrangedSubview(cell)
    }
    /// Shared image gallery: every player sees the actual unlocked/locked cast.
    /// A new virtual-coin purchase always requires a second explicit approval.
    private func characterGallery(in stack:UIStackView,refresh:@escaping()->Void) {
        // Keep canonical skin indices (and existing saves) unchanged. The
        // Three new bird pilots retain original skin slot IDs 6–8.
        let displayOrder=[6,7,8,0,1,2,3,4,5]
        for start in stride(from:0,to:displayOrder.count,by:3) {
            let row=UIStackView()
            row.axis = .horizontal;row.alignment = .fill
            row.distribution = .fillEqually;row.spacing=5
            stack.addArrangedSubview(row)
            for position in start..<min(start+3,displayOrder.count) {
                let i=displayOrder[position]
                let selected=progress.skinIndex()==i
                let owned=progress.owned(i)
                let cost=progress.costs[i]
                let status=selected ? "✓ ODABRAN" : owned ? "DOSTUPAN" : i >= 7 ? "PREMIUM · \(cost) KOVANICA" : "\(cost) KOVANICA"
                let card=BopaviActionButton(primary:selected)
                card.setTitle("",for:.normal)
                card.layer.borderWidth=selected ? 3 : 1
                card.layer.borderColor=(selected
                    ? UIColor(red:1,green:0.84,blue:0.34,alpha:1)
                    : UIColor(red:0.43,green:0.72,blue:0.94,alpha:0.7)).cgColor
                card.accessibilityLabel="\(progress.skinNames[i]), \(status)"
                card.heightAnchor.constraint(equalToConstant:174).isActive=true
                card.isSelected=selected
                // Interactive card with layered scene/portrait and a true
                // local ownership/currency caption, not an image of a UI.
                let scene=UIView()
                scene.translatesAutoresizingMaskIntoConstraints=false
                scene.layer.cornerRadius=14
                scene.clipsToBounds=true
                card.addSubview(scene)
                let scenery=UIImageView(image:UIImage(named:"World\(i % 8)"))
                scenery.translatesAutoresizingMaskIntoConstraints=false
                scenery.contentMode = .scaleAspectFill
                scenery.clipsToBounds=true
                scenery.alpha=owned ? 0.86 : 0.66
                scenery.accessibilityElementsHidden=true
                scene.addSubview(scenery)
                let tint=UIView()
                tint.translatesAutoresizingMaskIntoConstraints=false
                tint.backgroundColor=UIColor(red:0.01,green:0.08,blue:0.21,alpha:0.32)
                tint.accessibilityElementsHidden=true
                scene.addSubview(tint)
                let portrait=UIImageView(image:UIImage(named:"BopiPortrait\(i)"))
                portrait.translatesAutoresizingMaskIntoConstraints=false
                portrait.contentMode = .scaleAspectFit
                portrait.alpha=owned ? 1 : 0.88
                portrait.accessibilityElementsHidden=true
                scene.addSubview(portrait)
                let badge=UILabel()
                badge.translatesAutoresizingMaskIntoConstraints=false
                badge.text=selected ? "♛" : (!owned ? "🔒" : "✦")
                badge.font=UIFont.systemFont(ofSize:16,weight:.heavy)
                badge.textColor=selected ? UIColor(red:1,green:0.85,blue:0.3,alpha:1) : .white
                badge.backgroundColor=UIColor(red:0.02,green:0.17,blue:0.40,alpha:0.95)
                badge.textAlignment = .center
                badge.layer.cornerRadius=15
                badge.layer.borderWidth=1
                badge.layer.borderColor=UIColor(red:0.31,green:0.78,blue:1,alpha:1).cgColor
                badge.clipsToBounds=true
                badge.accessibilityElementsHidden=true
                scene.addSubview(badge)
                let name=UILabel()
                name.translatesAutoresizingMaskIntoConstraints=false
                name.text=progress.skinNames[i].uppercased()
                name.font=UIFont.systemFont(ofSize:13,weight:.black)
                name.textColor = .white
                name.textAlignment = .center
                name.adjustsFontSizeToFitWidth=true
                name.minimumScaleFactor=0.68
                name.shadowColor=UIColor.black.withAlphaComponent(0.8)
                name.shadowOffset=CGSize(width:0,height:2)
                name.accessibilityElementsHidden=true
                scene.addSubview(name)
                let price=UILabel()
                price.translatesAutoresizingMaskIntoConstraints=false
                price.text=status
                price.font=UIFont.systemFont(ofSize:10,weight:.heavy)
                price.textColor=selected ? UIColor(red:1,green:0.88,blue:0.48,alpha:1)
                    : owned ? UIColor(red:0.5,green:1,blue:0.78,alpha:1)
                    : UIColor(red:0.83,green:0.9,blue:1,alpha:1)
                price.textAlignment = .center
                price.adjustsFontSizeToFitWidth=true
                price.minimumScaleFactor=0.68
                price.backgroundColor=UIColor(red:0.02,green:0.1,blue:0.25,alpha:0.96)
                price.layer.cornerRadius=11
                price.clipsToBounds=true
                price.accessibilityElementsHidden=true
                card.addSubview(price)
                NSLayoutConstraint.activate([
                    scene.topAnchor.constraint(equalTo:card.topAnchor,constant:4),
                    scene.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:3),
                    scene.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-3),
                    scene.heightAnchor.constraint(equalToConstant:132),
                    scenery.leadingAnchor.constraint(equalTo:scene.leadingAnchor),
                    scenery.trailingAnchor.constraint(equalTo:scene.trailingAnchor),
                    scenery.topAnchor.constraint(equalTo:scene.topAnchor),
                    scenery.bottomAnchor.constraint(equalTo:scene.bottomAnchor),
                    tint.leadingAnchor.constraint(equalTo:scene.leadingAnchor),
                    tint.trailingAnchor.constraint(equalTo:scene.trailingAnchor),
                    tint.topAnchor.constraint(equalTo:scene.topAnchor),
                    tint.bottomAnchor.constraint(equalTo:scene.bottomAnchor),
                    portrait.leadingAnchor.constraint(equalTo:scene.leadingAnchor,constant:2),
                    portrait.trailingAnchor.constraint(equalTo:scene.trailingAnchor,constant:-2),
                    portrait.topAnchor.constraint(equalTo:scene.topAnchor,constant:2),
                    portrait.heightAnchor.constraint(equalToConstant:112),
                    badge.trailingAnchor.constraint(equalTo:scene.trailingAnchor,constant:-4),
                    badge.topAnchor.constraint(equalTo:scene.topAnchor,constant:4),
                    badge.heightAnchor.constraint(equalToConstant:30),
                    badge.widthAnchor.constraint(equalToConstant:30),
                    name.leadingAnchor.constraint(equalTo:scene.leadingAnchor,constant:4),
                    name.trailingAnchor.constraint(equalTo:scene.trailingAnchor,constant:-4),
                    name.bottomAnchor.constraint(equalTo:scene.bottomAnchor,constant:-3),
                    name.heightAnchor.constraint(equalToConstant:30),
                    price.topAnchor.constraint(equalTo:scene.bottomAnchor,constant:4),
                    price.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:4),
                    price.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-4),
                    price.heightAnchor.constraint(equalToConstant:28)
                ])
                card.addAction(UIAction{ [weak self] _ in
                    guard let self=self else{return}
                    self.sound.effect("click")
                    if owned {
                        _=self.progress.selectOrBuy(i)
                        refresh()
                    } else if self.progress.coins()<cost {
                        self.alert("Nedovoljno kovanica","Prikupi još kovanica za \(self.progress.skinNames[i]).")
                    } else {
                        let confirmation=UIAlertController(
                            title:"Otključati \(self.progress.skinNames[i])?",
                            message:"Potrošit ćeš \(cost) osvojenih kovanica. Potvrdi otključavanje.",
                            preferredStyle:.alert)
                        confirmation.addAction(UIAlertAction(title:"Odustani",style:.cancel))
                        confirmation.addAction(UIAlertAction(title:"Otključaj",style:.default){_ in
                            if self.progress.selectOrBuy(i) {refresh()}
                        })
                        self.present(confirmation,animated:true)
                    }
                },for:.touchUpInside)
                row.addArrangedSubview(card)
            }
            // Exactly nine characters, three columns on all supported iPhones.
        }
    }
    /// Character choice is a deliberate step before gameplay or boost consumption.
    private func showPilotPicker(_ world:Int,_ number:Int) {
        gameWorld=world;gameNumber=number
        let s=menu("ODABERI LIKA","Izaberi letača prije početka svakog leta")
        let selected=progress.skinIndex()
        // World backdrop, dimming gradient and selected pilot are actual
        // native image layers. UI text/buttons are never part of a bitmap.
        let preview=UIView()
        preview.translatesAutoresizingMaskIntoConstraints=false
        preview.backgroundColor=UIColor(red:0.03,green:0.18,blue:0.43,alpha:1)
        preview.layer.cornerRadius=24
        preview.clipsToBounds=true
        preview.heightAnchor.constraint(equalToConstant:216).isActive=true
        s.addArrangedSubview(preview)
        let worldBackdrop=UIImageView(image:UIImage(named:"World\(world)"))
        worldBackdrop.contentMode = .scaleAspectFill
        worldBackdrop.alpha=0.80
        worldBackdrop.isAccessibilityElement=false
        worldBackdrop.translatesAutoresizingMaskIntoConstraints=false
        preview.addSubview(worldBackdrop)
        let shade=UIView()
        shade.translatesAutoresizingMaskIntoConstraints=false
        shade.backgroundColor=UIColor(red:0.01,green:0.10,blue:0.27,alpha:0.42)
        shade.isAccessibilityElement=false
        preview.addSubview(shade)
        let portrait=UIImageView(image:UIImage(named:"BopiPortrait\(selected)"))
        portrait.translatesAutoresizingMaskIntoConstraints=false
        portrait.contentMode = .scaleAspectFit
        portrait.isAccessibilityElement=true
        portrait.accessibilityLabel="Pregled lika \(progress.skinNames[selected])"
        preview.addSubview(portrait)
        // Animating the preview CALayer cannot affect the live flight model.
        // Reduced Motion shows the same fully readable static preview.
        if !UIAccessibility.isReduceMotionEnabled {
            let pilotHover=CABasicAnimation(keyPath:"transform.translation.y")
            pilotHover.fromValue=0
            pilotHover.toValue = -8
            pilotHover.duration=1.4
            pilotHover.autoreverses=true
            pilotHover.repeatCount = .infinity
            pilotHover.timingFunction=CAMediaTimingFunction(name:.easeInEaseOut)
            portrait.layer.add(pilotHover,forKey:"pilotHover")
        }
        let badge=UILabel()
        badge.text="♛  ODABRAN"
        badge.textAlignment = .center
        badge.font=UIFont.systemFont(ofSize:12,weight:.heavy)
        badge.textColor = .white
        badge.backgroundColor=UIColor(red:0.80,green:0.45,blue:0.02,alpha:0.96)
        badge.layer.cornerRadius=17
        badge.layer.borderWidth=2
        badge.layer.borderColor=UIColor(red:1,green:0.90,blue:0.50,alpha:1).cgColor
        badge.clipsToBounds=true
        badge.isAccessibilityElement=false
        badge.translatesAutoresizingMaskIntoConstraints=false
        preview.addSubview(badge)
        NSLayoutConstraint.activate([
            worldBackdrop.leadingAnchor.constraint(equalTo:preview.leadingAnchor),
            worldBackdrop.trailingAnchor.constraint(equalTo:preview.trailingAnchor),
            worldBackdrop.topAnchor.constraint(equalTo:preview.topAnchor),
            worldBackdrop.bottomAnchor.constraint(equalTo:preview.bottomAnchor),
            shade.leadingAnchor.constraint(equalTo:preview.leadingAnchor),
            shade.trailingAnchor.constraint(equalTo:preview.trailingAnchor),
            shade.topAnchor.constraint(equalTo:preview.topAnchor),
            shade.bottomAnchor.constraint(equalTo:preview.bottomAnchor),
            portrait.leadingAnchor.constraint(equalTo:preview.leadingAnchor,constant:14),
            portrait.trailingAnchor.constraint(equalTo:preview.trailingAnchor,constant:-14),
            portrait.topAnchor.constraint(equalTo:preview.topAnchor),
            portrait.bottomAnchor.constraint(equalTo:preview.bottomAnchor,constant:-10),
            badge.trailingAnchor.constraint(equalTo:preview.trailingAnchor,constant:-10),
            badge.topAnchor.constraint(equalTo:preview.topAnchor,constant:10),
            badge.widthAnchor.constraint(equalToConstant:122),
            badge.heightAnchor.constraint(equalToConstant:35)
        ])
        label(progress.skinNames[selected],26,UIColor(red:1,green:0.86,blue:0.49,alpha:1),s)
        switch selected {
        case 6: label("Vihor: tirkizni nebeski pilot sa zlatnim naočalama i munjevitim krilima.",15,.white,s)
        case 7: label("Iskra: ružičasta kolibrica sa zlatnim naočalama, cvijetom i koraljnim perjem.",15,.white,s)
        case 8: label("Orion: ljubičasti noćni sova-pilot sa zvjezdanim krilima i zlatnim očima.",15,.white,s)
        default: label("Izaberi svog letača. Odabir se čuva na uređaju.",15,.white,s)
        }
        label("\(BopaviCore.names[world]) · Level \(number) · \(progress.coins()) kovanica",15,.white,s)
        sectionHeading("ODABERI SVOG LETAČA",in:s)
        characterGallery(in:s){self.showPilotPicker(world,number)}
        // Match Android's selection-first workflow; never launch before
        // explicit confirmation after the complete nine-pilot gallery.
        button("▶  POLETI S \(progress.skinNames[selected].uppercased())",in:s) {
            self.startGame(world,number)
        }
        button("‹  LEVELI",in:s,primary:false){self.showLevels(world,page:min(number,BopaviCore.levelsPerWorld))}
    }

    /// All elements are real UIKit views and actions. A full-screen dimmer
    /// intercepts touches while physics remains frozen; only Continue resumes.
    private func showPremiumPause(for canvas:GameCanvas){
        let overlay=UIView()
        overlay.translatesAutoresizingMaskIntoConstraints=false
        overlay.backgroundColor=UIColor(red:0,green:0.055,blue:0.14,alpha:0.80)
        view.addSubview(overlay)
        NSLayoutConstraint.activate([
            overlay.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            overlay.trailingAnchor.constraint(equalTo:view.trailingAnchor),
            overlay.topAnchor.constraint(equalTo:view.topAnchor),
            overlay.bottomAnchor.constraint(equalTo:view.bottomAnchor)
        ])
        let scroll=UIScrollView()
        scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.showsVerticalScrollIndicator=false
        scroll.alwaysBounceVertical=true
        overlay.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:overlay.safeAreaLayoutGuide.topAnchor,constant:5),
            scroll.bottomAnchor.constraint(equalTo:overlay.safeAreaLayoutGuide.bottomAnchor,constant:-5),
            scroll.leadingAnchor.constraint(equalTo:overlay.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:overlay.trailingAnchor)
        ])
        let panel=UIStackView()
        panel.axis = .vertical
        panel.alignment = .fill
        panel.spacing=9
        panel.translatesAutoresizingMaskIntoConstraints=false
        panel.isLayoutMarginsRelativeArrangement=true
        panel.layoutMargins=UIEdgeInsets(top:15,left:17,bottom:20,right:17)
        panel.backgroundColor=UIColor(red:0.015,green:0.16,blue:0.37,alpha:0.98)
        panel.layer.cornerRadius=27
        panel.layer.borderWidth=3
        panel.layer.borderColor=UIColor(red:0.12,green:0.83,blue:1,alpha:1).cgColor
        scroll.addSubview(panel)
        NSLayoutConstraint.activate([
            panel.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor,constant:13),
            panel.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor,constant:-13),
            panel.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor,constant:8),
            panel.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor,constant:-8),
            panel.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor,constant:-26)
        ])
        let mascot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        mascot.contentMode = .scaleAspectFit
        mascot.isAccessibilityElement=false
        mascot.heightAnchor.constraint(equalToConstant:130).isActive=true
        panel.addArrangedSubview(mascot)
        label("♛  PAUZA",33,UIColor.white,panel)
        label("Pauza · Level \(canvas.game.displayLevel)",14,
              UIColor(red:0.75,green:0.90,blue:1,alpha:1),panel)
        let close:(Bool)->Void = {[weak overlay,weak canvas,weak self] resume in
            overlay?.removeFromSuperview()
            if resume {canvas?.paused=false;self?.sound.resume()}
        }
        func command(_ caption:String,style:Int,handler:@escaping()->Void){
            let control:UIButton
            if style==2 {
                let exit=UIButton(type:.system)
                exit.backgroundColor=UIColor(red:0.22,green:0.34,blue:0.57,alpha:1)
                exit.layer.cornerRadius=26
                exit.layer.borderWidth=2
                exit.layer.borderColor=UIColor(red:0.65,green:0.77,blue:0.93,alpha:1).cgColor
                control=exit
            } else {
                control=BopaviActionButton(primary:style==0)
            }
            control.setTitle(caption,for:.normal)
            control.setTitleColor(.white,for:.normal)
            control.titleLabel?.font=UIFont.systemFont(ofSize:18,weight:.heavy)
            control.titleLabel?.adjustsFontSizeToFitWidth=true
            control.titleLabel?.minimumScaleFactor=0.7
            control.accessibilityLabel=caption
            control.heightAnchor.constraint(equalToConstant:59).isActive=true
            control.addAction(UIAction{_ in self.sound.effect("click");handler()},for:.touchUpInside)
            panel.addArrangedSubview(control)
        }
        command("▶  NASTAVI",style:0){close(true)}
        command("↻  PONOVO",style:1){
            close(false);self.startGame(self.gameWorld,self.gameNumber)
        }
        command("↪  IZLAZ",style:2){close(false);self.showHome()}
        // Paused in-place settings: navigating to the standalone Settings screen
        // destroys the live canvas. Keep the original simulation and overlay.
        let pauseSettings=UIStackView()
        pauseSettings.axis = .vertical
        pauseSettings.alignment = .fill
        pauseSettings.spacing=8
        pauseSettings.isLayoutMarginsRelativeArrangement=true
        pauseSettings.layoutMargins=UIEdgeInsets(top:12,left:10,bottom:12,right:10)
        pauseSettings.backgroundColor=UIColor(red:0.025,green:0.15,blue:0.34,alpha:1)
        pauseSettings.layer.cornerRadius=19
        pauseSettings.layer.borderWidth=2
        pauseSettings.layer.borderColor=UIColor(red:0.24,green:0.80,blue:1,alpha:1).cgColor
        pauseSettings.isHidden=true
        label("POSTAVKE U PAUZI",17,.white,pauseSettings)
        volumeSlider("♫  Glazba",value:progress.musicVolume,in:pauseSettings){value in
            self.progress.musicVolume=value
            self.sound.musicVolume=Float(value)/100
            self.sound.pause()
        }
        volumeSlider("◖  Zvukovi",value:progress.effectsVolume,in:pauseSettings){value in
            self.progress.effectsVolume=value
            self.sound.effectsVolume=Float(value)/100
            self.sound.pause()
        }
        let pauseHaptic=UISwitch()
        pauseHaptic.isOn=progress.hapticEnabled
        pauseHaptic.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        pauseHaptic.accessibilityLabel="Vibracije"
        pauseHaptic.addAction(UIAction{_ in self.progress.hapticEnabled=pauseHaptic.isOn},
                              for:.valueChanged)
        let hapticRow=UIStackView()
        hapticRow.axis = .horizontal
        hapticRow.alignment = .center
        hapticRow.spacing=10
        hapticRow.isLayoutMarginsRelativeArrangement=true
        hapticRow.layoutMargins=UIEdgeInsets(top:9,left:12,bottom:9,right:12)
        hapticRow.backgroundColor=UIColor(red:0.025,green:0.13,blue:0.30,alpha:1)
        hapticRow.layer.cornerRadius=15
        let hapticLabel=UILabel()
        hapticLabel.text="Vibracije"
        hapticLabel.textColor = .white
        hapticLabel.font=UIFont.systemFont(ofSize:16,weight:.medium)
        hapticLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        hapticRow.addArrangedSubview(hapticLabel)
        hapticRow.addArrangedSubview(pauseHaptic)
        pauseSettings.addArrangedSubview(hapticRow)
        label("Promjene se spremaju odmah. Let ostaje pauziran.",13,
              UIColor(red:0.78,green:0.91,blue:1,alpha:1),pauseSettings)
        let footer=UIStackView()
        footer.axis = .horizontal
        footer.distribution = .fillEqually
        footer.spacing=5
        footer.layoutMargins=UIEdgeInsets(top:7,left:5,bottom:7,right:5)
        footer.isLayoutMarginsRelativeArrangement=true
        footer.backgroundColor=UIColor(red:0.01,green:0.13,blue:0.31,alpha:1)
        footer.layer.cornerRadius=17
        func quick(_ caption:String,label:String,handler:@escaping()->Void){
            let control=UIButton(type:.system)
            let suffix:String
            if label=="Zvuk" {suffix=self.progress.soundEnabled ? " ✓":" ✕"}
            else if label=="Muzika" {suffix=self.progress.musicVolume>0 ? " ✓":" ✕"}
            else {suffix=""}
            control.setTitle(caption+suffix,for:.normal)
            control.titleLabel?.numberOfLines=2
            control.titleLabel?.textAlignment = .center
            control.titleLabel?.font=UIFont.systemFont(ofSize:11,weight:.bold)
            control.titleLabel?.adjustsFontSizeToFitWidth=true
            control.titleLabel?.minimumScaleFactor=0.67
            control.setTitleColor(.white,for:.normal)
            control.accessibilityLabel=label
            control.backgroundColor=UIColor(red:0.03,green:0.35,blue:0.68,alpha:1)
            control.layer.cornerRadius=13
            control.layer.borderWidth=1
            control.layer.borderColor=UIColor(red:0.37,green:0.83,blue:1,alpha:1).cgColor
            control.heightAnchor.constraint(equalToConstant:64).isActive=true
            control.addAction(UIAction{[weak control] _ in
                guard let control=control else{return}
                self.sound.effect("click");handler()
                if label=="Zvuk" || label=="Muzika" {
                    let active=label=="Zvuk" ? self.progress.soundEnabled : self.progress.musicVolume>0
                    control.setTitle(caption+(active ? " ✓":" ✕"),for:.normal)
                    control.accessibilityLabel=label+(active ? " uključeno":" isključeno")
                }
            },for:.touchUpInside)
            footer.addArrangedSubview(control)
        }
        quick("◖\nZVUK",label:"Zvuk"){
            self.progress.soundEnabled.toggle()
            self.sound.enabled=self.progress.soundEnabled
            self.sound.pause() // Restore no-audio state until Continue.
        }
        quick("♫\nMUZIKA",label:"Muzika"){
            let volume=self.progress.toggleMusicVolume()
            self.sound.musicVolume=Float(volume)/100
            self.sound.pause()
        }
        quick("✦\nKONTROLE",label:"Kontrole"){
            self.alert("Kontrole leta",canvas.game.dragControl
                ? "Povuci prst gore ili dolje kako bi Bopi glatko mijenjao visinu. Izbjegavaj prepreke i skupljaj nagrade."
                : "Dodirni zaslon za zamah krilima. Pauza se pojavljuje tek nakon početka leta.")
        }
        quick("⚙\nPOSTAVKE",label:"Postavke"){
            pauseSettings.isHidden.toggle()
            self.sound.pause()
            if !pauseSettings.isHidden {
                scroll.layoutIfNeeded()
                let bottom=max(0,scroll.contentSize.height-scroll.bounds.height)
                scroll.setContentOffset(CGPoint(x:0,y:bottom),animated:true)
                UIAccessibility.post(notification:.layoutChanged,argument:pauseSettings)
            } else {
                UIAccessibility.post(notification:.layoutChanged,argument:footer)
            }
        }
        panel.addArrangedSubview(footer)
        panel.addArrangedSubview(pauseSettings)
        UIAccessibility.post(notification:.screenChanged,argument:panel)
    }
    private func startGame(_ world:Int,_ number:Int){
        clear();gameWorld=world;gameNumber=number
        let boosts=progress.previewPerks()
        let game=GameSimulation(BopaviCore.createStream(world,number),endless:true,initialShield:boosts.shield,initialMagnet:boosts.magnet,difficulty:progress.difficulty,initialOrdinal:number)
        sound.startWorld(world)
        let gameCanvas=GameCanvas(game:game,reducedMotion:progress.lessMotion,skinIndex:progress.skinIndex())
        canvas=gameCanvas
        setNeedsStatusBarAppearanceUpdate()
        gameCanvas.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(gameCanvas)
        NSLayoutConstraint.activate([gameCanvas.topAnchor.constraint(equalTo:view.topAnchor),gameCanvas.bottomAnchor.constraint(equalTo:view.bottomAnchor),gameCanvas.leadingAnchor.constraint(equalTo:view.leadingAnchor),gameCanvas.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        gameCanvas.onFinished = { [weak self,weak gameCanvas] g in
            guard let self=self, self.canvas === gameCanvas else{return}
            self.showResult(g)
        }
        gameCanvas.onFlap = { [weak self] in self?.sound.effect("tap") }
        gameCanvas.onCollect = { [weak self] in
            self?.sound.effect("collect")
            if self?.progress.hapticEnabled == true {self?.collectHaptic.selectionChanged()}
        }
        gameCanvas.onShieldImpact = { [weak self] in
            guard let self=self else {return}
            self.sound.effect("hit")
            if self.progress.hapticEnabled {self.shieldHaptic.impactOccurred()}
        }
        gameCanvas.onLevelComplete = { [weak self] level in
            guard let self=self else{return}
            self.progress.recordLevelStars(world:world,number:level,earned:game.completedLevelStars)
            let reward=self.progress.completeLevel(world:world,number:level,score:game.score())
            self.sound.effect("level")
            if reward>0 {self.showToast("Level \(level): +\(reward) kovanica")}
        }
        // Gameplay counts now share one frame-synchronous Core Graphics HUD
        // on Android and iOS; avoid a duplicate UIKit chip over the scene.
        let pause=UIButton(type:.system);pause.setTitle("Ⅱ",for:.normal);pause.titleLabel?.font=UIFont.boldSystemFont(ofSize:24)
        pause.backgroundColor=UIColor(red:0.11,green:0.24,blue:0.41,alpha:0.96);pause.setTitleColor(.white,for:.normal)
        pause.layer.cornerRadius=16;pause.layer.borderWidth=1
        pause.layer.borderColor=UIColor.white.withAlphaComponent(0.3).cgColor
        pause.accessibilityLabel="Izbornik tijekom igre"
        // Keep pause out of the idle flight preview and VoiceOver tree.
        pause.isHidden=true
        pause.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(pause)
        gameCanvas.onFlightStarted = { [weak self,weak gameCanvas,weak pause] in
            guard let self=self, self.canvas === gameCanvas else{return}
            // The pre-flight preview never spends purchased equipment.
            _ = self.progress.consumePerks()
            pause?.isHidden=false
        }
        NSLayoutConstraint.activate([pause.trailingAnchor.constraint(equalTo:view.trailingAnchor,constant:-15),pause.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:12),pause.heightAnchor.constraint(equalToConstant:48),pause.widthAnchor.constraint(equalToConstant:58)])
        pause.addAction(UIAction{[weak self,weak gameCanvas] _ in
            guard let self=self,let canvas=gameCanvas,
                  self.canvas === canvas,canvas.game.active && !canvas.game.finished else{return}
            canvas.paused=true;self.sound.pause()
            self.showPremiumPause(for:canvas)

        },for:.touchUpInside)
    }
    private func showToast(_ message:String){
        guard view.window != nil else{return}
        let notice=UILabel();notice.text=message;notice.textAlignment = .center;notice.textColor = .white
        notice.font=UIFont.systemFont(ofSize:15,weight:.bold);notice.backgroundColor=UIColor(red:0.06,green:0.27,blue:0.43,alpha:0.92)
        notice.layer.cornerRadius=12;notice.clipsToBounds=true
        notice.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(notice)
        NSLayoutConstraint.activate([notice.centerXAnchor.constraint(equalTo:view.centerXAnchor),notice.centerYAnchor.constraint(equalTo:view.centerYAnchor,constant:-110),notice.widthAnchor.constraint(lessThanOrEqualTo:view.widthAnchor,multiplier:0.85),notice.heightAnchor.constraint(equalToConstant:44)])
        DispatchQueue.main.asyncAfter(deadline:.now()+1.5){[weak notice] in notice?.removeFromSuperview()}
    }
    private func showResult(_ g:GameSimulation){
        let previousRecord=progress.bestPoints()
        let newRecord=g.score()>previousRecord && g.score()>0
        sound.effect(g.won ? "level" : "hit")
        let headline=ResultHeadline.label(score:g.score(),previousBest:previousRecord,won:g.won)
        progress.recordRun(g)
        gameNumber=g.displayLevel
        clear()
        view.backgroundColor=UIColor(red:0.09,green:0.39,blue:0.73,alpha:1)
        if let image=UIImage(named:"World\(gameWorld)") {
            let backdrop=UIImageView(image:image)
            backdrop.contentMode = .scaleAspectFill
            backdrop.clipsToBounds=true;backdrop.translatesAutoresizingMaskIntoConstraints=false
            backdrop.isAccessibilityElement=false;view.addSubview(backdrop)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
        }
        let dim=UIView()
        dim.backgroundColor=UIColor(red:0.01,green:0.08,blue:0.21,alpha:0.78)
        dim.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(dim)
        NSLayoutConstraint.activate([
            dim.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            dim.trailingAnchor.constraint(equalTo:view.trailingAnchor),
            dim.topAnchor.constraint(equalTo:view.topAnchor),
            dim.bottomAnchor.constraint(equalTo:view.bottomAnchor)
        ])
        let scroll=UIScrollView();scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.showsVerticalScrollIndicator=false
        scroll.alwaysBounceVertical=true
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:view.trailingAnchor)
        ])
        let stack=UIStackView()
        stack.axis = .vertical;stack.alignment = .fill;stack.spacing=10
        stack.isLayoutMarginsRelativeArrangement=true
        stack.layoutMargins=UIEdgeInsets(top:22,left:24,bottom:26,right:24)
        stack.translatesAutoresizingMaskIntoConstraints=false
        scroll.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            stack.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            stack.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            stack.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor),
            stack.heightAnchor.constraint(greaterThanOrEqualTo:scroll.frameLayoutGuide.heightAnchor)
        ])
        // Same live visual composition as Android: real selected pilot and
        // world image, not a mocked result screenshot or static generic hero.
        let pilotIndex=progress.skinIndex()
        let header=UIView()
        header.clipsToBounds=true
        header.layer.cornerRadius=22
        header.layer.borderWidth=2
        header.layer.borderColor=UIColor(red:0.34,green:0.81,blue:1,alpha:1).cgColor
        header.heightAnchor.constraint(equalToConstant:192).isActive=true
        header.isAccessibilityElement=true
        header.accessibilityLabel="Odabrani letač: \(progress.skinNames[pilotIndex]), svijet \(BopaviCore.names[gameWorld])"
        stack.addArrangedSubview(header)
        let scene=UIImageView(image:UIImage(named:"World\(gameWorld)"))
        scene.translatesAutoresizingMaskIntoConstraints=false
        scene.contentMode = .scaleAspectFill
        scene.clipsToBounds=true
        scene.isAccessibilityElement=false
        header.addSubview(scene)
        let fade=UIView()
        fade.translatesAutoresizingMaskIntoConstraints=false
        fade.backgroundColor=UIColor(red:0.02,green:0.09,blue:0.26,alpha:0.32)
        fade.isAccessibilityElement=false
        header.addSubview(fade)
        let pilot=UIImageView(image:UIImage(named:"BopiPortrait\(pilotIndex)"))
        pilot.translatesAutoresizingMaskIntoConstraints=false
        pilot.contentMode = .scaleAspectFit
        pilot.isAccessibilityElement=false
        header.addSubview(pilot)
        NSLayoutConstraint.activate([
            scene.leadingAnchor.constraint(equalTo:header.leadingAnchor),
            scene.trailingAnchor.constraint(equalTo:header.trailingAnchor),
            scene.topAnchor.constraint(equalTo:header.topAnchor),
            scene.bottomAnchor.constraint(equalTo:header.bottomAnchor),
            fade.leadingAnchor.constraint(equalTo:header.leadingAnchor),
            fade.trailingAnchor.constraint(equalTo:header.trailingAnchor),
            fade.topAnchor.constraint(equalTo:header.topAnchor),
            fade.bottomAnchor.constraint(equalTo:header.bottomAnchor),
            pilot.leadingAnchor.constraint(equalTo:header.leadingAnchor,constant:6),
            pilot.trailingAnchor.constraint(equalTo:header.trailingAnchor,constant:-6),
            pilot.centerYAnchor.constraint(equalTo:header.centerYAnchor),
            pilot.heightAnchor.constraint(equalToConstant:186)
        ])
        label(headline,31,UIColor(red:1,green:0.86,blue:0.38,alpha:1),stack)
        if newRecord {
            let banner=UILabel()
            banner.text="♛  NOVI REKORD!"
            banner.textColor=UIColor(red:1,green:0.93,blue:0.67,alpha:1)
            banner.textAlignment = .center
            banner.font=UIFont.systemFont(ofSize:19,weight:.heavy)
            banner.backgroundColor=UIColor(red:0.42,green:0.22,blue:0.04,alpha:0.96)
            banner.layer.cornerRadius=20
            banner.layer.borderWidth=2
            banner.layer.borderColor=UIColor(red:1,green:0.79,blue:0.26,alpha:1).cgColor
            banner.clipsToBounds=true
            banner.heightAnchor.constraint(equalToConstant:46).isActive=true
            banner.accessibilityLabel="Novi osobni rekord: \(g.score()) bodova"
            stack.addArrangedSubview(banner)
        }
        let sky=UIColor(red:0.67,green:0.92,blue:1,alpha:1)
        let gold=UIColor(red:1,green:0.82,blue:0.28,alpha:1)
        let summary=UIStackView()
        summary.axis = .horizontal;summary.alignment = .fill
        summary.distribution = .fillEqually;summary.spacing=6
        stack.addArrangedSubview(summary)
        // All summary values are derived from the real finished GameSimulation.
        func resultCard(_ caption:String,icon:String,value:String,highlight:UIColor)->UILabel {
            let card=UIStackView()
            card.axis = .vertical;card.alignment = .fill;card.spacing=3
            card.isLayoutMarginsRelativeArrangement=true
            card.layoutMargins=UIEdgeInsets(top:8,left:3,bottom:9,right:3)
            card.backgroundColor=UIColor(red:0.025,green:0.15,blue:0.34,alpha:0.97)
            card.layer.cornerRadius=19;card.layer.borderWidth=2
            card.layer.borderColor=UIColor(red:0.22,green:0.79,blue:1,alpha:1).cgColor
            card.heightAnchor.constraint(equalToConstant:139).isActive=true
            summary.addArrangedSubview(card)
            let iconView=label(icon,29,gold,card)
            iconView.heightAnchor.constraint(equalToConstant:37).isActive=true
            iconView.isAccessibilityElement=false
            let title=label(caption,11,.white,card)
            title.numberOfLines=2;title.adjustsFontSizeToFitWidth=true
            title.minimumScaleFactor=0.80
            title.heightAnchor.constraint(equalToConstant:34).isActive=true
            title.isAccessibilityElement=false
            let amount=label(value,23,highlight,card)
            amount.adjustsFontSizeToFitWidth=true
            amount.minimumScaleFactor=0.67
            amount.accessibilityLabel="\(caption) \(value)"
            amount.heightAnchor.constraint(equalToConstant:32).isActive=true
            return amount
        }
        _=resultCard("REZULTAT",icon:"★",value:String(g.score()),highlight:gold)
        _=resultCard("PRIKUPLJENE\nKOVANICE",icon:"●",value:String(g.coins),highlight:sky)
        _=resultCard("UKUPNO\nPROLAZA",icon:"⚑",value:String(g.totalPassed),highlight:.white)
        // Mirror Android's condensed readable detail block, leaving room
        // for the primary Retry and result navigation on compact iPhones.
        let details=label("Najbolji rezultat: \(progress.bestPoints())\n" +
            "Level \(gameNumber) · Ukupno prolaza: \(g.totalPassed)\n" +
            "Težina: \(progress.difficultyNames[g.difficulty]) · \(progress.playerName)",
            14,.white,stack)
        details.numberOfLines=3
        details.lineBreakMode = .byWordWrapping
        let balance=label("Stanje novčanika: \(progress.coins()) kovanica",15,sky,stack)
        sectionHeading("♛  NAGRADE",in:stack)
        // No fake diamonds, chests, purchases or video rewards. The displayed
        // coins/stars are pickups; the wallet balance remains independently real.
        let rewardPanel=UIStackView()
        rewardPanel.axis = .horizontal;rewardPanel.alignment = .fill
        rewardPanel.distribution = .fillEqually;rewardPanel.spacing=4
        rewardPanel.isLayoutMarginsRelativeArrangement=true
        rewardPanel.layoutMargins=UIEdgeInsets(top:7,left:4,bottom:7,right:4)
        rewardPanel.backgroundColor=UIColor(red:0.015,green:0.15,blue:0.34,alpha:1)
        rewardPanel.layer.cornerRadius=23;rewardPanel.layer.borderWidth=2
        rewardPanel.layer.borderColor=UIColor(red:0.22,green:0.80,blue:1,alpha:1).cgColor
        stack.addArrangedSubview(rewardPanel)
        func reward(_ name:String,icon:String,value:String)->UILabel {
            let card=UIStackView()
            card.axis = .vertical;card.alignment = .fill;card.spacing=2
            card.isLayoutMarginsRelativeArrangement=true
            card.layoutMargins=UIEdgeInsets(top:7,left:2,bottom:7,right:2)
            card.backgroundColor=UIColor(red:0.02,green:0.23,blue:0.46,alpha:1)
            card.layer.cornerRadius=15;card.layer.borderWidth=1
            card.layer.borderColor=UIColor(red:0.28,green:0.64,blue:0.90,alpha:1).cgColor
            card.heightAnchor.constraint(equalToConstant:114).isActive=true
            rewardPanel.addArrangedSubview(card)
            let picture=label(icon,25,gold,card)
            picture.heightAnchor.constraint(equalToConstant:35).isActive=true
            picture.isAccessibilityElement=false
            let caption=label(name,10,.white,card)
            caption.heightAnchor.constraint(equalToConstant:27).isActive=true
            caption.numberOfLines=2;caption.adjustsFontSizeToFitWidth=true
            caption.minimumScaleFactor=0.78
            caption.isAccessibilityElement=false
            let amount=label(value,17,sky,card)
            amount.heightAnchor.constraint(equalToConstant:28).isActive=true
            amount.adjustsFontSizeToFitWidth=true;amount.minimumScaleFactor=0.70
            amount.accessibilityLabel="\(name) \(value)"
            return amount
        }
        _=reward("KOVANICE",icon:"●",value:"×\(g.coins)")
        _=reward("ZVIJEZDE",icon:"★",value:"×\(g.stars)")
        let bonus=progress.bonusCoinsAvailable()
        let bonusCounter=reward("BONUS\nBODOVA",icon:"♛",value:"+\(bonus)")
        _=reward("REKORD",icon:"🏆",value:String(progress.bestPoints()))
        if bonus>0 {
            let claim=BopaviActionButton(primary:false)
            claim.setTitle("🎁  PREUZMI \(bonus) KOVANICA ZA REKORD",for:.normal)
            claim.accessibilityLabel="Preuzmi bonus kovanica za rekord"
            claim.heightAnchor.constraint(equalToConstant:56).isActive=true
            claim.addAction(UIAction{[weak self,weak claim] _ in
                guard let self=self else{return}
                let awarded=self.progress.claimBonusCoins()
                if awarded>0 {
                    self.sound.effect("purchase")
                    bonusCounter.text="+0"
                    bonusCounter.accessibilityLabel="BONUS BODOVA 0"
                    balance.text="Stanje novčanika: \(self.progress.coins()) kovanica"
                    claim?.isHidden=true
                }
            },for:.touchUpInside)
            stack.addArrangedSubview(claim)
        }
        button("▶  PONOVO",in:stack){self.showPilotPicker(self.gameWorld,self.gameNumber)}
        let footer=UIStackView()
        footer.axis = .horizontal;footer.distribution = .fillEqually
        footer.spacing=6;stack.addArrangedSubview(footer)
        func footerAction(_ caption:String,handler:@escaping()->Void){
            let control=UIButton(type:.system)
            control.setTitle(caption,for:.normal)
            control.setTitleColor(.white,for:.normal)
            control.titleLabel?.numberOfLines=2
            control.titleLabel?.textAlignment = .center
            control.titleLabel?.font=UIFont.systemFont(ofSize:13,weight:.bold)
            control.titleLabel?.adjustsFontSizeToFitWidth=true
            control.titleLabel?.minimumScaleFactor=0.72
            control.layer.cornerRadius=17
            control.layer.borderWidth=2
            control.layer.borderColor=UIColor(red:0.57,green:0.84,blue:1,alpha:1).cgColor
            control.backgroundColor=UIColor(red:0.10,green:0.32,blue:0.65,alpha:1)
            control.accessibilityLabel=caption
            control.heightAnchor.constraint(equalToConstant:57).isActive=true
            control.addAction(UIAction{_ in self.sound.effect("click");handler()},for:.touchUpInside)
            footer.addArrangedSubview(control)
        }
        footerAction("▥\nLJESTVICA"){self.showLeaderboard()}
        footerAction("⌂\nPOVRATAK"){self.showHome()}
        footerAction("↗\nPODIJELI"){
            let message="BOPAVI · Rezultat \(g.score()) · Ukupno prolaza: \(g.totalPassed)"
            let share=UIActivityViewController(activityItems:[message],applicationActivities:nil)
            share.popoverPresentationController?.sourceView=self.view
            share.popoverPresentationController?.sourceRect=CGRect(
                x:self.view.bounds.midX,y:self.view.bounds.midY,width:1,height:1)
            self.present(share,animated:true)
        }
        button("MAPA SVJETOVA",in:stack,primary:false){self.showWorlds()}
        button("🛍  TRGOVINA KOVANICAMA",in:stack,primary:false){self.showPerks(.home)}

    }
    private func showPerks(){showPerks(.settings)}
    private func showPerks(_ origin:MenuOrigin){
        let s=menu("TRGOVINA","Za kovanice osvojene igrom — bez stvarnog novca")
        label("●  \(progress.coins()) KOVANICA",24,UIColor(red:1,green:0.86,blue:0.44,alpha:1),s)
        label("Za svakih novih 1.000 bodova najboljeg rezultata dobivaš 1 kovanicu.",15,.white,s)
        let bonus=progress.bonusCoinsAvailable()
        if bonus>0 {
            button("🎁  PREUZMI \(bonus) KOVANICA ZA BODOVE",in:s,primary:false){
                let earned=self.progress.claimBonusCoins()
                if earned>0 {self.sound.effect("purchase");self.showPerks(origin)}
            }
        }

        label("Osvajaj kovanice prelaskom nagradnih levela i biraj opremu za sljedeći let.",15,.white,s)
        for n in 0..<2 {
            let extra=n==0 ? "Čuva Bopija od jednog sudara" : "Privlači kovanice i predmete 8 sekundi"
            label("\(n==0 ? "🛡 ŠTIT" : "🧲 MAGNET") · \(extra) · U torbi: \(progress.perkCount(n))",16,.white,s)
            button("KUPI \(n==0 ? "ŠTIT" : "MAGNET") · \(progress.perkPrices[n]) KOVANICA",in:s,primary:false){
                if self.progress.buyPerk(n){self.sound.effect("purchase");self.showPerks(origin)}
                else {self.alert("Kupnja nije moguća","Nedovoljno kovanica ili je zaliha puna.")}
            }
        }
        label("Kupljena oprema automatski se koristi na početku sljedećeg leta.",14,.white,s)
        button("🎨  LIKOVI",in:s,primary:false){self.showSkins(.shop,shopOrigin:origin)}
        button(origin == .home ? "‹  POČETNA" : "‹  POSTAVKE",in:s,primary:false){
            if origin == .home {self.showHome()} else {self.showSettings()}
        }
    }
    private func showSkins(){showSkins(.settings)}
    private func showSkins(_ origin:MenuOrigin,shopOrigin:MenuOrigin = .settings){
        let s=menu("LIKOVI","Devet krilatih pilota: Vihor, Iskra i Orion")
        label("Stanje: \(progress.coins()) kovanica",20,.white,s)
        characterGallery(in:s){self.showSkins(origin,shopOrigin:shopOrigin)}
        let backTitle=origin == .home ? "POČETNA" : origin == .profile ? "PROFIL"
            : origin == .shop ? "TRGOVINA" : "POSTAVKE"
        button("‹  "+backTitle,in:s,primary:false){
            switch origin {
            case .home: self.showHome()
            case .settings: self.showSettings()
            case .profile: self.showProfile()
            case .shop: self.showPerks(shopOrigin)
            }
        }
    }

    /// All profile values are derived from local save state; no fabricated account.
    private func showProfile(){
        let s=menu("PROFIL","Tvoj letački dnevnik i osobni rezultati")
        let skin=progress.skinIndex()
        let hero=UIStackView()
        hero.axis = .horizontal;hero.alignment = .center;hero.spacing=8
        hero.layoutMargins=UIEdgeInsets(top:11,left:8,bottom:11,right:8)
        hero.isLayoutMarginsRelativeArrangement=true
        hero.backgroundColor=UIColor(red:0.035,green:0.16,blue:0.38,alpha:0.98)
        hero.layer.cornerRadius=24
        hero.layer.borderWidth=2
        hero.layer.borderColor=UIColor(red:0.0,green:0.82,blue:1,alpha:1).cgColor
        hero.accessibilityIdentifier="profile-live-card"
        let portraitBox=UIStackView()
        portraitBox.axis = .vertical;portraitBox.spacing=2
        portraitBox.alignment = .fill
        portraitBox.backgroundColor=UIColor(red:0.03,green:0.31,blue:0.68,alpha:1)
        portraitBox.layer.cornerRadius=20
        portraitBox.layer.borderWidth=2
        portraitBox.layer.borderColor=UIColor(red:1,green:0.76,blue:0.30,alpha:1).cgColor
        portraitBox.isAccessibilityElement=true
        portraitBox.accessibilityLabel="Letački profil, "+progress.playerName+", lik "+progress.skinNames[skin]
        portraitBox.widthAnchor.constraint(equalToConstant:124).isActive=true
        let portrait=UIImageView(image:UIImage(named:"BopiPortrait"+String(skin)))
        portrait.contentMode = .scaleAspectFit
        portrait.heightAnchor.constraint(equalToConstant:151).isActive=true
        portrait.isAccessibilityElement=false
        portraitBox.addArrangedSubview(portrait)
        let levelLabel=label("LEVEL "+String(progress.streamFrontier(progress.chosenWorld())),
            11,UIColor(red:1,green:0.77,blue:0.30,alpha:1),portraitBox)
        levelLabel.heightAnchor.constraint(equalToConstant:27).isActive=true
        // Gauge the selected world's *saved* stars. No invented XP, metres
        // or account level: it is a real 0...30 world completion indicator.
        let profileWorld=progress.chosenWorld()
        let profileStars=max(0,min(30,progress.worldStars(profileWorld)))
        let starGauge=UIProgressView(progressViewStyle:.default)
        starGauge.progress=Float(profileStars)/30
        starGauge.progressTintColor=UIColor(red:1,green:0.76,blue:0.30,alpha:1)
        starGauge.trackTintColor=UIColor(red:0.06,green:0.24,blue:0.49,alpha:1)
        starGauge.transform=CGAffineTransform(scaleX:1,y:3)
        starGauge.heightAnchor.constraint(equalToConstant:12).isActive=true
        starGauge.isAccessibilityElement=true
        starGauge.accessibilityLabel="Zvjezdice svijeta "+BopaviCore.names[profileWorld]
        starGauge.accessibilityValue="\(profileStars) od 30"
        portraitBox.addArrangedSubview(starGauge)
        let starCaption=label("★  \(profileStars)/30 · "+BopaviCore.names[profileWorld],10,.white,portraitBox)
        starCaption.numberOfLines=1
        starCaption.adjustsFontSizeToFitWidth=true;starCaption.minimumScaleFactor=0.8
        starCaption.heightAnchor.constraint(equalToConstant:27).isActive=true
        hero.addArrangedSubview(portraitBox)
        let stats=UIStackView()
        stats.axis = .vertical;stats.alignment = .fill;stats.spacing=3
        let pilotName=label(progress.playerName.uppercased(),20,.white,stats)
        pilotName.textAlignment = .left
        pilotName.adjustsFontSizeToFitWidth=true;pilotName.minimumScaleFactor=0.55
        pilotName.numberOfLines=1
        let pilotSubtitle=label(progress.skinNames[skin]+" · pilot",12,
            UIColor(red:0.78,green:0.91,blue:1,alpha:1),stats)
        pilotSubtitle.textAlignment = .left
        let eyebrow=label("TVOJI REZULTATI",11,
            UIColor(red:1,green:0.77,blue:0.30,alpha:1),stats)
        eyebrow.textAlignment = .left
        let completed=(0..<8).reduce(Int64(0)) { sum,world in
            sum+Int64(progress.frontier(world)-1)
        }
        let actualStats:[(String,String)]=[
            ("★  Najbolji rezultat",String(progress.bestPoints())),
            ("●  Osvojene kovanice",String(progress.coins())),
            ("♛  Dovršeni leveli",String(completed)),
            ("⚑  Pobjede",String(progress.wins()))
        ]
        for (caption,value) in actualStats {
            let tile=UIStackView()
            tile.axis = .horizontal;tile.alignment = .center;tile.spacing=3
            tile.isLayoutMarginsRelativeArrangement=true
            tile.layoutMargins=UIEdgeInsets(top:5,left:4,bottom:5,right:4)
            tile.backgroundColor=UIColor(red:0.035,green:0.17,blue:0.38,alpha:1)
            tile.layer.cornerRadius=12
            tile.isAccessibilityElement=true
            tile.accessibilityLabel=caption+": "+value
            let textLabel=UILabel()
            textLabel.text=caption;textLabel.textColor = .white
            textLabel.font=UIFont.systemFont(ofSize:10,weight:.semibold)
            textLabel.minimumScaleFactor=0.65
            textLabel.adjustsFontSizeToFitWidth=true
            textLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
            let metric=UILabel()
            metric.text=value
            metric.textColor=UIColor(red:1,green:0.77,blue:0.30,alpha:1)
            metric.font=UIFont.monospacedDigitSystemFont(ofSize:14,weight:.heavy)
            metric.setContentCompressionResistancePriority(.required,for:.horizontal)
            tile.addArrangedSubview(textLabel);tile.addArrangedSubview(metric)
            tile.heightAnchor.constraint(equalToConstant:35).isActive=true
            stats.addArrangedSubview(tile)
        }
        hero.addArrangedSubview(stats)
        s.addArrangedSubview(hero)
        label("Odabrani svijet: "+BopaviCore.names[progress.chosenWorld()],15,.white,s)
        sectionHeading("MOJE NAGRADE",in:s)
        let explored=(0..<8).filter{progress.frontier($0)>1}.count
        let collected=(0..<8).reduce(Int64(0)) { sum,w in
            sum+Int64(progress.collectibles(w))
        }
        let awards:[(String,String,Bool)]=[
            ("✦","PRVI LET",progress.wins()>0),
            ("🌍","ISTRAŽIVAČ",explored>=5),
            ("♛","SAKUPLJAČ",collected>=500),
            ("⚑","VETERAN",progress.wins()>=10)
        ]
        let badgeRow=UIStackView()
        badgeRow.axis = .horizontal;badgeRow.distribution = .fillEqually;badgeRow.spacing=4
        for (icon,name,earned) in awards {
            let requirement:String
            switch name {
            case "PRVI LET": requirement="1 pobjeda"
            case "ISTRAŽIVAČ": requirement="5 svjetova"
            case "SAKUPLJAČ": requirement="500 predmeta"
            default: requirement="10 pobjeda"
            }
            let tile=UIStackView()
            tile.axis = .vertical;tile.alignment = .fill
            tile.isLayoutMarginsRelativeArrangement=true
            tile.layoutMargins=UIEdgeInsets(top:5,left:2,bottom:5,right:2)
            tile.backgroundColor=earned
                ? UIColor(red:0.07,green:0.30,blue:0.63,alpha:1)
                : UIColor(red:0.15,green:0.21,blue:0.32,alpha:1)
            tile.layer.cornerRadius=15;tile.layer.borderWidth=2
            tile.layer.borderColor=(earned
                ? UIColor(red:1,green:0.78,blue:0.30,alpha:1)
                : UIColor(red:0.45,green:0.52,blue:0.65,alpha:1)).cgColor
            tile.isAccessibilityElement=true
            tile.accessibilityLabel=name+", cilj "+requirement+", "+(earned ? "osvojeno":"zaključano")
            let glyph=label(earned ? icon:"🔒",24,
                UIColor(red:1,green:0.77,blue:0.30,alpha:1),tile)
            glyph.heightAnchor.constraint(equalToConstant:39).isActive=true
            let caption=label(name,10,.white,tile)
            caption.numberOfLines=1
            caption.adjustsFontSizeToFitWidth=true
            caption.minimumScaleFactor=0.72
            caption.heightAnchor.constraint(equalToConstant:23).isActive=true
            let goal=label(requirement,9,UIColor(red:0.77,green:0.91,blue:1,alpha:1),tile)
            goal.numberOfLines=1;goal.adjustsFontSizeToFitWidth=true
            goal.minimumScaleFactor=0.75
            goal.heightAnchor.constraint(equalToConstant:22).isActive=true
            tile.heightAnchor.constraint(equalToConstant:101).isActive=true
            badgeRow.addArrangedSubview(tile)
        }
        s.addArrangedSubview(badgeRow)
        button("★  POGLEDAJ SVA POSTIGNUĆA",in:s,primary:false){
            self.showAchievements(.profile)
        }
        sectionHeading("MOJI NAJBOLJI SVJETOVI",in:s)
        let ranked=(0..<8).sorted {
            let left=progress.worldStars($0),right=progress.worldStars($1)
            if left != right{return left>right}
            let bestLeft=progress.best($0),bestRight=progress.best($1)
            return bestLeft == bestRight ? $0<$1 : bestLeft>bestRight
        }
        let worldRow=UIStackView()
        worldRow.axis = .horizontal;worldRow.distribution = .fillEqually;worldRow.spacing=6
        for (rank,world) in ranked.prefix(3).enumerated(){
            let tile=UIButton(type:.custom)
            tile.layer.cornerRadius=15;tile.clipsToBounds=true
            tile.layer.borderWidth=2
            tile.layer.borderColor=UIColor(red:0.0,green:0.83,blue:1,alpha:1).cgColor
            tile.backgroundColor=UIColor(red:0.03,green:0.23,blue:0.50,alpha:1)
            tile.accessibilityLabel=BopaviCore.names[world] + ", " + String(progress.worldStars(world)) + " od 30 zvjezdica; otvori levele"
            let content=UIStackView()
            content.translatesAutoresizingMaskIntoConstraints=false
            content.axis = .vertical;content.spacing=0
            content.isUserInteractionEnabled=false
            tile.addSubview(content)
            NSLayoutConstraint.activate([
                content.leadingAnchor.constraint(equalTo:tile.leadingAnchor),
                content.trailingAnchor.constraint(equalTo:tile.trailingAnchor),
                content.topAnchor.constraint(equalTo:tile.topAnchor),
                content.bottomAnchor.constraint(equalTo:tile.bottomAnchor)
            ])
            let artFrame=UIView()
            artFrame.clipsToBounds=true
            artFrame.heightAnchor.constraint(equalToConstant:81).isActive=true
            let art=UIImageView(image:UIImage(named:"World"+String(world)))
            art.translatesAutoresizingMaskIntoConstraints=false
            art.contentMode = .scaleAspectFill;art.clipsToBounds=true
            artFrame.addSubview(art)
            let rankMedal=UILabel()
            rankMedal.translatesAutoresizingMaskIntoConstraints=false
            rankMedal.text="♛ \(rank+1)"
            rankMedal.textAlignment = .center
            rankMedal.font=UIFont.systemFont(ofSize:12,weight:.heavy)
            rankMedal.textColor=UIColor(red:1,green:0.77,blue:0.30,alpha:1)
            rankMedal.backgroundColor=UIColor(red:0.04,green:0.16,blue:0.39,alpha:0.95)
            rankMedal.layer.cornerRadius=14;rankMedal.layer.masksToBounds=true
            rankMedal.layer.borderWidth=1
            rankMedal.layer.borderColor=UIColor(red:1,green:0.77,blue:0.30,alpha:1).cgColor
            rankMedal.isAccessibilityElement=false
            artFrame.addSubview(rankMedal)
            NSLayoutConstraint.activate([
                art.leadingAnchor.constraint(equalTo:artFrame.leadingAnchor),
                art.trailingAnchor.constraint(equalTo:artFrame.trailingAnchor),
                art.topAnchor.constraint(equalTo:artFrame.topAnchor),
                art.bottomAnchor.constraint(equalTo:artFrame.bottomAnchor),
                rankMedal.leadingAnchor.constraint(equalTo:artFrame.leadingAnchor,constant:5),
                rankMedal.topAnchor.constraint(equalTo:artFrame.topAnchor,constant:5),
                rankMedal.widthAnchor.constraint(equalToConstant:46),
                rankMedal.heightAnchor.constraint(equalToConstant:28)
            ])
            content.addArrangedSubview(artFrame)
            let title=label(BopaviCore.names[world].uppercased(),11,.white,content)
            title.numberOfLines=1;title.adjustsFontSizeToFitWidth=true
            title.minimumScaleFactor=0.72
            title.heightAnchor.constraint(equalToConstant:26).isActive=true
            let stars=label("★ "+String(progress.worldStars(world))+"/30",12,
                UIColor(red:1,green:0.77,blue:0.30,alpha:1),content)
            stars.heightAnchor.constraint(equalToConstant:25).isActive=true
            tile.heightAnchor.constraint(equalToConstant:132).isActive=true
            tile.addAction(UIAction{_ in
                self.sound.effect("click");self.showLevels(world,page:1)
            },for:.touchUpInside)
            worldRow.addArrangedSubview(tile)
        }
        s.addArrangedSubview(worldRow)
        button("✎  UREDI PROFIL",in:s){self.showSettings()}
        button("✦  ODABERI DRUGOG LIKA",in:s,primary:false){self.showSkins(.profile)}
        let footer=UIStackView()
        footer.axis = .horizontal;footer.distribution = .fillEqually;footer.spacing=5
        footer.isLayoutMarginsRelativeArrangement=true
        footer.layoutMargins=UIEdgeInsets(top:6,left:5,bottom:6,right:5)
        footer.backgroundColor=UIColor(red:0.012,green:0.13,blue:0.32,alpha:0.96)
        footer.layer.cornerRadius=22;footer.layer.borderWidth=2
        footer.layer.borderColor=UIColor(red:0.0,green:0.82,blue:1,alpha:1).cgColor
        let tabs:[(String,String,()->Void)]=[
            ("NavProfile","PROFIL",{self.showProfile()}),
            ("NavTasks","ZADACI",{self.showAchievements(.profile)}),
            ("NavCollection","KOLEKCIJA",{self.showSkins(.profile)}),
            ("NavShop","TRGOVINA",{self.showPerks(.profile)})
        ]
        for (index,item) in tabs.enumerated(){
            let tab=UIButton(type:.custom)
            tab.backgroundColor=index==0
                ? UIColor(red:0.60,green:0.36,blue:0.05,alpha:1)
                : UIColor(red:0.04,green:0.31,blue:0.63,alpha:0.8)
            tab.layer.cornerRadius=13
            tab.heightAnchor.constraint(equalToConstant:59).isActive=true
            tab.accessibilityLabel=item.1+(index==0 ? ", odabrano":"")
            let content=UIStackView()
            content.axis = .vertical;content.translatesAutoresizingMaskIntoConstraints=false
            content.isUserInteractionEnabled=false
            let glyph=UIImageView(image:UIImage(named:item.0))
            glyph.contentMode = .scaleAspectFit
            glyph.heightAnchor.constraint(equalToConstant:28).isActive=true
            content.addArrangedSubview(glyph)
            let name=label(item.1,11,.white,content)
            name.adjustsFontSizeToFitWidth=true;name.minimumScaleFactor=0.72
            name.heightAnchor.constraint(equalToConstant:22).isActive=true
            tab.addSubview(content)
            NSLayoutConstraint.activate([
                content.centerYAnchor.constraint(equalTo:tab.centerYAnchor),
                content.leadingAnchor.constraint(equalTo:tab.leadingAnchor,constant:2),
                content.trailingAnchor.constraint(equalTo:tab.trailingAnchor,constant:-2)
            ])
            tab.addAction(UIAction{_ in self.sound.effect("click");item.2()},for:.touchUpInside)
            footer.addArrangedSubview(tab)
        }
        s.addArrangedSubview(footer)
        button("‹  POČETNA",in:s,primary:false){self.showHome()}
    }

    private func showLeaderboard(){
        let stack=menu("MOJI REKORDI","Tvojih deset najboljih letova")
        let entries=progress.leaderboard()
        if let best=entries.first {
            let banner=UIStackView()
            banner.axis = .vertical;banner.alignment = .fill;banner.spacing=3
            banner.isLayoutMarginsRelativeArrangement=true
            banner.layoutMargins=UIEdgeInsets(top:15,left:13,bottom:17,right:13)
            banner.backgroundColor=UIColor(red:0.025,green:0.29,blue:0.64,alpha:0.98)
            banner.layer.cornerRadius=23
            banner.layer.borderWidth=3
            banner.layer.borderColor=UIColor(red:1,green:0.78,blue:0.29,alpha:1).cgColor
            banner.isAccessibilityElement=true
            banner.accessibilityLabel="Osobni rekord: \(best.score) bodova"
            label("♛  OSOBNI REKORD",18,UIColor(red:1,green:0.79,blue:0.32,alpha:1),banner)
            let score=label("\(best.score) BODOVA",29,.white,banner)
            score.font=UIFont.monospacedDigitSystemFont(ofSize:29,weight:.heavy)
            stack.addArrangedSubview(banner)
        } else {
            label("Prvi rekord čeka tvoj sljedeći let!",17,.white,stack)
        }
        for (index,item) in entries.enumerated() {
            let champion=index==0
            let card=UIStackView()
            card.axis = .horizontal;card.alignment = .center;card.spacing=9
            card.isLayoutMarginsRelativeArrangement=true
            card.layoutMargins=UIEdgeInsets(top:12,left:12,bottom:12,right:12)
            card.backgroundColor=UIColor(red:0.025,green:champion ? 0.29 : 0.16,
                                         blue:champion ? 0.55 : 0.37,alpha:1)
            card.layer.cornerRadius=18;card.layer.borderWidth=champion ? 2 : 1
            card.layer.borderColor=champion
                ? UIColor(red:1,green:0.78,blue:0.29,alpha:1).cgColor
                : UIColor(red:0.10,green:0.78,blue:1,alpha:1).cgColor
            card.isAccessibilityElement=true
            card.accessibilityLabel="Let \(index+1), \(item.score) bodova, \(BopaviCore.names[item.world]), \(item.gates) prolaza"
            let medal=UILabel()
            medal.text=champion ? "♛" : String(index+1)
            medal.textAlignment = .center;medal.font=UIFont.systemFont(ofSize:champion ? 29:22,weight:.heavy)
            medal.textColor=champion ? UIColor(red:1,green:0.77,blue:0.30,alpha:1) : .white
            medal.widthAnchor.constraint(equalToConstant:49).isActive=true
            medal.heightAnchor.constraint(equalToConstant:50).isActive=true
            card.addArrangedSubview(medal)
            let detail=UIStackView()
            detail.axis = .vertical;detail.alignment = .fill;detail.spacing=2
            let main=label("\(item.score) BODOVA",20,.white,detail)
            main.font=UIFont.monospacedDigitSystemFont(ofSize:20,weight:.heavy)
            main.adjustsFontSizeToFitWidth=true;main.minimumScaleFactor=0.72
            let location=label("\(BopaviCore.names[item.world]) · \(progress.difficultyNames[item.difficulty])",12,
                               UIColor(red:0.83,green:0.94,blue:1,alpha:1),detail)
            location.adjustsFontSizeToFitWidth=true;location.minimumScaleFactor=0.7
            card.addArrangedSubview(detail)
            let gates=UILabel()
            gates.text="⚑ \(item.gates)";gates.textColor=UIColor(red:1,green:0.77,blue:0.30,alpha:1)
            gates.font=UIFont.monospacedDigitSystemFont(ofSize:13,weight:.heavy)
            gates.setContentCompressionResistancePriority(.required,for:.horizontal)
            card.addArrangedSubview(gates)
            stack.addArrangedSubview(card)
        }
        button("★  MOJA POSTIGNUĆA",in:stack,primary:false){self.showAchievements()}
        button("‹  POSTAVKE",in:stack,primary:false){self.showSettings()}
    }
    private func showAchievements(){showAchievements(.settings)}
    private func showAchievements(_ origin:MenuOrigin){
        let s=menu("POSTIGNUĆA","Nagrade koje si osvojio tijekom igre")
        let total=(0..<8).reduce(0){$0+self.progress.frontier($1)-1}
        label("Dovršeni leveli: \(total)",19,.white,s)
        label("Pobjede: \(progress.wins()) · Pokušaji bez pobjede: \(progress.deaths())",17,.white,s)
         for w in 0..<8 {label("\(BopaviCore.names[w]) · najbolji rezultat \(progress.best(w))",16,.white,s)}
        let backTitle=origin == .home ? "POČETNA" : origin == .profile ? "PROFIL" : "POSTAVKE"
        button("‹  "+backTitle,in:s,primary:false){
            switch origin {
            case .home: self.showHome()
            case .profile: self.showProfile()
            default: self.showSettings()
            }
        }
    }
    private func volumeSlider(_ name:String,value:Int,in stack:UIStackView,onValue:@escaping(Int)->Void){
        let panel=UIStackView()
        panel.axis = .vertical;panel.alignment = .fill;panel.spacing=4
        panel.isLayoutMarginsRelativeArrangement=true
        panel.layoutMargins=UIEdgeInsets(top:10,left:15,bottom:10,right:15)
        panel.backgroundColor=UIColor(red:0.025,green:0.13,blue:0.30,alpha:0.97)
        panel.layer.cornerRadius=16
        panel.layer.borderWidth=1
        panel.layer.borderColor=UIColor(red:0.24,green:0.74,blue:1,alpha:0.55).cgColor
        let header=UIStackView()
        header.axis = .horizontal;header.distribution = .fill
        let title=UILabel()
        title.text=name;title.textColor = .white
        title.font=UIFont.systemFont(ofSize:16,weight:.bold)
        let percent=UILabel()
        percent.text="\(value)%";percent.textColor=UIColor(red:1,green:0.77,blue:0.30,alpha:1)
        percent.font=UIFont.monospacedDigitSystemFont(ofSize:16,weight:.bold)
        percent.textAlignment = .right
        header.addArrangedSubview(title)
        header.addArrangedSubview(percent)
        panel.addArrangedSubview(header)
        let slider=UISlider()
        slider.minimumValue=0
        slider.maximumValue=100
        slider.value=Float(value)
        slider.accessibilityLabel=name
        slider.minimumTrackTintColor=UIColor(red:0.01,green:0.72,blue:0.97,alpha:1)
        slider.maximumTrackTintColor=UIColor(red:0.10,green:0.28,blue:0.51,alpha:1)
        slider.thumbTintColor=UIColor(red:1,green:0.79,blue:0.27,alpha:1)
        slider.heightAnchor.constraint(greaterThanOrEqualToConstant:36).isActive=true
        slider.addAction(UIAction{_ in
            let updated=max(0,min(100,Int(slider.value.rounded())))
            percent.text="\(updated)%"
            onValue(updated)
        },for:.valueChanged)
        panel.addArrangedSubview(slider)
        stack.addArrangedSubview(panel)
    }
    private func showSettings(){
        let s=menu("POSTAVKE","Prilagodi igru svom stilu")
        // The live settings are grouped over one deep-blue premium sheet.
        // The document remains scrollable and every control keeps its handler.
        let settingsPanel=BopaviSettingsPanel()
        s.addArrangedSubview(settingsPanel)
        sectionHeading("IZGLED I ZVUK",in:settingsPanel)
        volumeSlider("♫  Glazba",value:progress.musicVolume,in:settingsPanel){value in
            self.progress.musicVolume=value
            self.sound.musicVolume=Float(value)/100
        }
        volumeSlider("◖  Zvukovi",value:progress.effectsVolume,in:settingsPanel){value in
            self.progress.effectsVolume=value
            self.sound.effectsVolume=Float(value)/100
        }
        let haptic=UISwitch();haptic.isOn=progress.hapticEnabled
        haptic.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        let hapticRow=UIStackView();hapticRow.axis = .horizontal;hapticRow.spacing=12
        let hapticLabel=UILabel();hapticLabel.text="Vibracije"
        hapticLabel.font=UIFont.systemFont(ofSize:16,weight:.medium)
        hapticLabel.textColor = .white;hapticLabel.numberOfLines=0
        hapticLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        hapticRow.addArrangedSubview(hapticLabel);hapticRow.addArrangedSubview(haptic)
        hapticRow.isLayoutMarginsRelativeArrangement=true
        hapticRow.layoutMargins=UIEdgeInsets(top:11,left:16,bottom:11,right:16)
        hapticRow.backgroundColor=UIColor(red:0.025,green:0.13,blue:0.30,alpha:0.97)
        hapticRow.layer.cornerRadius=16
        hapticRow.layer.borderWidth=1
        hapticRow.layer.borderColor=UIColor(red:0.24,green:0.74,blue:1,alpha:0.65).cgColor
        settingsPanel.addArrangedSubview(hapticRow)
        haptic.addAction(UIAction{_ in self.progress.hapticEnabled=haptic.isOn},for:.valueChanged)
        sectionHeading("IGRAČ I TEŽINA",in:settingsPanel)
        label("TEŽINA IGRE — utječe na brzinu i gravitaciju",16,.white,settingsPanel)
        // Match Android's genuine three-card choice while preserving the
        // exact same difficulty 0/1/2 persisted by ProgressStore.
        let modeNames=["LAGANO","NORMALNO","ZAHTJEVNO"]
        let modeHints=["Opušten let","Uravnotežen izazov","Za iskusne pilote"]
        let modeArt=[0,6,3]
        let modeRow=UIStackView()
        modeRow.axis = .horizontal
        modeRow.alignment = .fill
        modeRow.distribution = .fillEqually
        modeRow.spacing=6
        var modeCards=[UIButton]()
        func refreshDifficultyCards(){
            let selected=self.progress.difficulty
            for (index,card) in modeCards.enumerated(){
                let chosen=index==selected
                card.backgroundColor=chosen
                    ? UIColor(red:0.36,green:0.21,blue:0.09,alpha:1)
                    : UIColor(red:0.035,green:0.17,blue:0.38,alpha:1)
                card.layer.borderWidth=chosen ? 3:2
                card.layer.borderColor=(chosen
                    ? UIColor(red:1,green:0.79,blue:0.36,alpha:1)
                    : UIColor(red:0.27,green:0.54,blue:0.84,alpha:1)).cgColor
                card.layer.shadowColor=UIColor(red:1,green:0.67,blue:0.18,alpha:1).cgColor
                card.layer.shadowOpacity=chosen ? 0.48:0
                card.layer.shadowRadius=chosen ? 9:0
                card.isSelected=chosen
                card.accessibilityLabel="Težina igre \(modeNames[index]), " +
                    "\(modeHints[index]), \(chosen ? "odabrano":"dodirni za odabir")"
            }
        }
        for mode in 0..<3 {
            let card=UIButton(type:.custom)
            card.layer.cornerRadius=19
            card.isAccessibilityElement=true
            card.accessibilityTraits=[.button]
            card.accessibilityIdentifier="difficulty-card-\(mode)"
            card.heightAnchor.constraint(equalToConstant:170).isActive=true
            let content=UIStackView()
            content.translatesAutoresizingMaskIntoConstraints=false
            content.axis = .vertical
            content.alignment = .fill
            content.spacing=1
            content.isUserInteractionEnabled=false
            card.addSubview(content)
            NSLayoutConstraint.activate([
                content.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:3),
                content.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-3),
                content.topAnchor.constraint(equalTo:card.topAnchor,constant:7),
                content.bottomAnchor.constraint(equalTo:card.bottomAnchor,constant:-9)
            ])
            let crown=UILabel()
            crown.text=mode==1 ? "♛":"✦"
            crown.font=UIFont.systemFont(ofSize:19,weight:.heavy)
            crown.textColor=UIColor(red:1,green:0.79,blue:0.36,alpha:1)
            crown.textAlignment = .center
            crown.heightAnchor.constraint(equalToConstant:23).isActive=true
            crown.isAccessibilityElement=false
            content.addArrangedSubview(crown)
            let portrait=UIImageView(image:UIImage(named:"BopiPortrait\(modeArt[mode])"))
            portrait.contentMode = .scaleAspectFit
            portrait.heightAnchor.constraint(equalToConstant:62).isActive=true
            portrait.isAccessibilityElement=false
            content.addArrangedSubview(portrait)
            let title=UILabel()
            title.text=modeNames[mode]
            title.font=UIFont.systemFont(ofSize:13,weight:.heavy)
            title.textColor = .white
            title.textAlignment = .center
            title.adjustsFontSizeToFitWidth=true
            title.minimumScaleFactor=0.76
            title.heightAnchor.constraint(equalToConstant:24).isActive=true
            title.isAccessibilityElement=false
            content.addArrangedSubview(title)
            let hint=UILabel()
            hint.text=modeHints[mode]
            hint.font=UIFont.systemFont(ofSize:11,weight:.medium)
            hint.textColor=UIColor(red:0.85,green:0.93,blue:1,alpha:1)
            hint.textAlignment = .center
            hint.numberOfLines=2
            hint.adjustsFontSizeToFitWidth=true
            hint.minimumScaleFactor=0.80
            hint.heightAnchor.constraint(equalToConstant:36).isActive=true
            hint.isAccessibilityElement=false
            content.addArrangedSubview(hint)
            card.addAction(UIAction{[weak self] _ in
                guard let self=self else{return}
                self.progress.difficulty=mode
                self.sound.effect("click")
                refreshDifficultyCards()
            },for:.touchUpInside)
            modeCards.append(card)
            modeRow.addArrangedSubview(card)
        }
        refreshDifficultyCards()
        settingsPanel.addArrangedSubview(modeRow)
        label("Težina se primjenjuje na sljedeći let. Dosadašnji napredak ostaje spremljen.",14,.white,settingsPanel)
        let player=UITextField()
        player.text=progress.playerName
        player.placeholder="Tvoje ime"
        player.textColor = .white
        player.backgroundColor=UIColor(red:0.02,green:0.15,blue:0.32,alpha:1)
        player.layer.cornerRadius=13
        player.layer.borderWidth=1
        player.layer.borderColor=UIColor(red:0.30,green:0.76,blue:1,alpha:0.7).cgColor
        player.heightAnchor.constraint(equalToConstant:52).isActive=true
        settingsPanel.addArrangedSubview(player)
        button("SPREMI IME",in:settingsPanel,primary:false){
            player.resignFirstResponder()
            self.progress.playerName=player.text ?? ""
            self.alert("Spremljeno","Ime igrača je promijenjeno.")
        }
        sectionHeading("DODATNE OPCIJE",in:settingsPanel)
        let toggle=UISwitch();toggle.isOn=progress.lessMotion
        toggle.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        let toggleRow=UIStackView();toggleRow.axis = .horizontal;toggleRow.spacing=12
        let l=UILabel();l.text="Nježnije animacije";l.font=UIFont.systemFont(ofSize:16,weight:.medium);l.textColor = .white
        l.numberOfLines=0;l.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        toggleRow.addArrangedSubview(l);toggleRow.addArrangedSubview(toggle);settingsPanel.addArrangedSubview(toggleRow)
        toggle.addAction(UIAction{_ in self.progress.lessMotion=toggle.isOn},for:.valueChanged)
        let audio=UISwitch();audio.isOn=progress.soundEnabled
        audio.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        let audioRow=UIStackView();audioRow.axis = .horizontal;audioRow.spacing=12
        let audioLabel=UILabel();audioLabel.text="Glazba i zvučni efekti";audioLabel.font=UIFont.systemFont(ofSize:16,weight:.medium);audioLabel.textColor = .white
        audioLabel.numberOfLines=0;audioLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        audioRow.addArrangedSubview(audioLabel);audioRow.addArrangedSubview(audio);settingsPanel.addArrangedSubview(audioRow)
        audio.addAction(UIAction{_ in self.progress.soundEnabled=audio.isOn;self.sound.enabled=audio.isOn},for:.valueChanged)

        button("🐤  LIKOVI",in:settingsPanel,primary:false){self.showSkins()}
        button("🛍  TRGOVINA KOVANICAMA",in:settingsPanel,primary:false){self.showPerks()}
        button("🏆  MOJI REKORDI",in:settingsPanel,primary:false){self.showLeaderboard()}
        sectionHeading("PODACI I PRIVATNOST",in:settingsPanel)
        label("BOPAVI — Mali let, velika avantura. Stvorio Brendigo.",14,.white,settingsPanel)
        label("Bez oglasa, praćenja i kupnje stvarnim novcem.",14,.white,settingsPanel)
        button("SPREMI KOPIJU NAPRETKA",in:settingsPanel,primary:false){
            do {
                let url=FileManager.default.temporaryDirectory.appendingPathComponent("bopavi-save.json")
                try self.progress.exportData().write(to:url,options:.atomic)
                let activity=UIActivityViewController(activityItems:[url],applicationActivities:nil)
                activity.popoverPresentationController?.sourceView=self.view
                self.present(activity,animated:true)
            } catch {self.alert("Izvoz nije uspio",error.localizedDescription)}
        }
        button("VRATI NAPREDAK IZ KOPIJE",in:settingsPanel,primary:false){
            let picker=UIDocumentPickerViewController(forOpeningContentTypes:[.json],asCopy:true)
            picker.delegate=self;self.present(picker,animated:true)
        }
        button("‹  POČETNA",in:s,primary:false){self.showHome()}
    }
    func documentPicker(_ controller:UIDocumentPickerViewController,didPickDocumentsAt urls:[URL]) {
        guard let url=urls.first else{return}
        let scoped=url.startAccessingSecurityScopedResource()
        defer{if scoped {url.stopAccessingSecurityScopedResource()}}
        do {
            // Read at most 550001 bytes from an untrusted document provider.
            let handle=try FileHandle(forReadingFrom:url)
            defer {try? handle.close()}
            let data=try handle.read(upToCount:550_001) ?? Data()
            try progress.importData(data)
            sound.enabled=progress.soundEnabled
            sound.musicVolume=Float(progress.musicVolume)/100
            sound.effectsVolume=Float(progress.effectsVolume)/100
            showHome();alert("Uspješno","Napredak je uvezen.")
        }
        catch {alert("Uvoz nije uspio",error.localizedDescription)}
    }
    private func alert(_ title:String,_ message:String){let a=UIAlertController(title:title,message:message,preferredStyle:.alert);a.addAction(UIAlertAction(title:"U redu",style:.default));present(a,animated:true)}
}
