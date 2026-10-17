import UIKit
import UniformTypeIdentifiers

/// Premium, self-contained control. The gradient resizes without a new image allocation.
private final class BopaviActionButton: UIButton {
    private let gradient = CAGradientLayer()
    init(primary:Bool,muted:Bool=false) {
        super.init(frame:.zero)
        gradient.colors = primary
            ? [UIColor(red:1.00,green:0.88,blue:0.32,alpha:1).cgColor,
               UIColor(red:1.00,green:0.67,blue:0.09,alpha:1).cgColor,
               UIColor(red:1.00,green:0.46,blue:0.00,alpha:1).cgColor]
            : muted
            ? [UIColor(red:0.57,green:0.72,blue:0.89,alpha:1).cgColor,
               UIColor(red:0.26,green:0.43,blue:0.67,alpha:1).cgColor,
               UIColor(red:0.12,green:0.23,blue:0.39,alpha:1).cgColor]
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
        let face=UIFont.systemFont(ofSize:18,weight:.heavy)
        titleLabel?.font=UIFont(descriptor:face.fontDescriptor.withDesign(.rounded)
            ?? face.fontDescriptor,size:18)
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
    private enum MenuOrigin { case home, settings, profile, shop, records }
    private let progress=ProgressStore()
    private let sound=Soundscape()
    private let collectHaptic=UISelectionFeedbackGenerator()
    private let shieldHaptic=UIImpactFeedbackGenerator(style:.medium)
    private var canvas:GameCanvas?
    private var recordsReturnToSettings=false
    private var currentMenuName="BOPAVI"
    private weak var pauseFitViewport:UIScrollView?
    private weak var pauseFitPanel:UIStackView?
    private weak var resultFitViewport:UIScrollView?
    private weak var resultFitPanel:UIStackView?
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
        // iOS already displays the branded native LaunchArt while UIKit loads.
        // The old extra runtime splash was replaced on the very next frame,
        // producing the duplicate logo/loading flash reported on real phones.
        // There is no asynchronous data to await here; create Home once.
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
    private func ui(_ text:String)->String {GameLanguages.text(text,progress.languageCode)}
    /// Fixed-size 2×4 paged native picker: no action-sheet scrolling,
    /// no external language libraries, no account or network access.
    private func showLanguagePicker(page:Int=0){
        let pageSize=8
        let pageCount=(GameLanguages.codes.count+pageSize-1)/pageSize
        let current=max(0,min(page,pageCount-1))
        let croatian=progress.languageCode=="hr"
        let sheet=UIViewController()
        sheet.modalPresentationStyle = .overFullScreen
        sheet.view.backgroundColor=UIColor(red:0.005,green:0.025,blue:0.08,alpha:0.88)
        let panel=UIStackView()
        panel.axis = .vertical
        panel.spacing=8
        panel.alignment = .fill
        panel.isLayoutMarginsRelativeArrangement=true
        panel.layoutMargins=UIEdgeInsets(top:17,left:13,bottom:14,right:13)
        panel.backgroundColor=UIColor(red:0.015,green:0.13,blue:0.33,alpha:1)
        panel.layer.cornerRadius=24
        panel.layer.borderWidth=2
        panel.layer.borderColor=UIColor(red:0.22,green:0.83,blue:1,alpha:1).cgColor
        panel.translatesAutoresizingMaskIntoConstraints=false
        sheet.view.addSubview(panel)
        NSLayoutConstraint.activate([
            panel.centerXAnchor.constraint(equalTo:sheet.view.centerXAnchor),
            panel.centerYAnchor.constraint(equalTo:sheet.view.centerYAnchor),
            panel.widthAnchor.constraint(equalToConstant:min(365,max(260,view.bounds.width-32))),
            panel.topAnchor.constraint(greaterThanOrEqualTo:sheet.view.safeAreaLayoutGuide.topAnchor,constant:8),
            panel.bottomAnchor.constraint(lessThanOrEqualTo:sheet.view.safeAreaLayoutGuide.bottomAnchor,constant:-8)
        ])
        let title=UILabel()
        title.text="\(ui("JEZIK"))   \(current+1)/\(pageCount)"
        title.textAlignment = .center
        title.textColor = .white
        title.font=UIFont.systemFont(ofSize:21,weight:.heavy)
        title.heightAnchor.constraint(equalToConstant:37).isActive=true
        panel.addArrangedSubview(title)
        func navButton(_ text:String,_ active:Bool,_ action:@escaping()->Void)->UIButton {
            let control=UIButton(type:.system)
            control.setTitle(text,for:.normal)
            control.setTitleColor(.white,for:.normal)
            control.titleLabel?.font=UIFont.systemFont(ofSize:13,weight:.bold)
            control.titleLabel?.adjustsFontSizeToFitWidth=true
            control.titleLabel?.minimumScaleFactor=0.75
            control.backgroundColor=UIColor(red:0.035,green:0.39,blue:0.72,alpha:1)
            control.layer.cornerRadius=13
            control.layer.borderWidth=1.5
            control.layer.borderColor=UIColor(red:0.29,green:0.88,blue:1,alpha:1).cgColor
            control.isEnabled=active
            control.alpha=active ? 1 : 0.40
            control.heightAnchor.constraint(greaterThanOrEqualToConstant:47).isActive=true
            control.addAction(UIAction{_ in action()},for:.touchUpInside)
            return control
        }
        for line in 0..<4 {
            let row=UIStackView()
            row.axis = .horizontal
            row.distribution = .fillEqually
            row.spacing=7
            panel.addArrangedSubview(row)
            for col in 0..<2 {
                let index=current*pageSize+line*2+col
                if index>=GameLanguages.codes.count {
                    let placeholder=UIView()
                    placeholder.heightAnchor.constraint(equalToConstant:47).isActive=true
                    row.addArrangedSubview(placeholder)
                    continue
                }
                let code=GameLanguages.codes[index]
                let selected=code==progress.languageCode
                let name=GameLanguages.names[index]
                let control=navButton((selected ? "✓ " : "")+name,true){[weak self,weak sheet] in
                    guard let self=self else{return}
                    self.progress.languageCode=code
                    sheet?.dismiss(animated:true){[weak self] in self?.showSettings()}
                }
                control.backgroundColor=selected ?
                    UIColor(red:0.72,green:0.40,blue:0.04,alpha:1) :
                    UIColor(red:0.035,green:0.31,blue:0.65,alpha:1)
                control.layer.borderColor=(selected ?
                    UIColor(red:1,green:0.83,blue:0.25,alpha:1) :
                    UIColor(red:0.28,green:0.82,blue:1,alpha:1)).cgColor
                control.accessibilityLabel=name
                if selected {control.accessibilityTraits.insert(.selected)}
                row.addArrangedSubview(control)
            }
        }
        let footer=UIStackView()
        footer.axis = .horizontal
        footer.distribution = .fillEqually
        footer.spacing=5
        footer.addArrangedSubview(navButton(croatian ? "PRETHODNO" : "PREV",current>0){
            [weak self,weak sheet] in
            sheet?.dismiss(animated:false){[weak self] in self?.showLanguagePicker(page:current-1)}
        })
        footer.addArrangedSubview(navButton(croatian ? "ODUSTANI" : "CANCEL",true){
            [weak sheet] in sheet?.dismiss(animated:true)
        })
        footer.addArrangedSubview(navButton(croatian ? "SLJEDEĆE" : "NEXT",current<pageCount-1){
            [weak self,weak sheet] in
            sheet?.dismiss(animated:false){[weak self] in self?.showLanguagePicker(page:current+1)}
        })
        panel.addArrangedSubview(footer)
        present(sheet,animated:true)
    }
    // Animate just the scene layer, never a hit target or live counter.
    // Honor both iOS accessibility and BOPAVI's own saved Reduced Motion.
    private func startSceneryDrift(_ backdrop:UIImageView) {
        guard !progress.lessMotion && !UIAccessibility.isReduceMotionEnabled else {return}
        let drift=CABasicAnimation(keyPath:"transform.scale")
        drift.fromValue=1.015
        drift.toValue=1.045
        drift.duration=12.5
        drift.autoreverses=true
        drift.repeatCount = .infinity
        drift.timingFunction=CAMediaTimingFunction(name:.easeInEaseOut)
        backdrop.layer.add(drift,forKey:"scenery-breathing")
    }
    private func menu(_ title:String,_ subtitle:String)->UIStackView {
        clear()
        currentMenuName=title
        if title != "BOPAVI" {
            let world=gameWorld >= 0 && gameWorld < 8 ? gameWorld : 0
            let backdrop=UIImageView(image:UIImage(named:"World\(world)"))
            backdrop.translatesAutoresizingMaskIntoConstraints=false
            backdrop.contentMode = .scaleAspectFill
            backdrop.clipsToBounds=true
            backdrop.isAccessibilityElement=false
            startSceneryDrift(backdrop)
            view.addSubview(backdrop)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
            let shade=UIView()
            // Brighter painted biomes; dark real controls retain their contrast.
            shade.backgroundColor=UIColor(red:0.012,green:0.08,blue:0.19,alpha:0.18)
            shade.translatesAutoresizingMaskIntoConstraints=false
            view.addSubview(shade)
            NSLayoutConstraint.activate([
                shade.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                shade.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                shade.topAnchor.constraint(equalTo:view.topAnchor),
                shade.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
            // The reference Worlds/Profile header features a second flying
            // Bopi behind live UI. Match the Android native layered approach.
            if title == "SVJETOVI" || title == "PROFIL" || title == "TRGOVINA" {
                let flyingPilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
                flyingPilot.contentMode = .scaleAspectFit
                flyingPilot.isAccessibilityElement=false
                flyingPilot.isUserInteractionEnabled=false
                flyingPilot.translatesAutoresizingMaskIntoConstraints=false
                view.addSubview(flyingPilot)
                NSLayoutConstraint.activate([
                    flyingPilot.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:37),
                    flyingPilot.trailingAnchor.constraint(equalTo:view.trailingAnchor,constant:-7),
                    flyingPilot.widthAnchor.constraint(equalToConstant:150),
                    flyingPilot.heightAnchor.constraint(equalToConstant:165)
                ])
            }
        }
        let scroll=UIScrollView()
        scroll.translatesAutoresizingMaskIntoConstraints=false
        // Extended Settings requires readable controls, not a downscaled
        // whole screen. Only this advanced Settings sheet scrolls; the normal
        // reference menus still fit inside the device's safe-area viewport.
        let isExtendedSettings = title == "POSTAVKE"
        scroll.alwaysBounceVertical=isExtendedSettings
        scroll.isScrollEnabled=isExtendedSettings
        scroll.showsVerticalScrollIndicator=isExtendedSettings
        scroll.keyboardDismissMode = .interactive
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        let stack=UIStackView()
        stack.axis = .vertical;stack.alignment = .fill
        let compact = title == "TRGOVINA" || title == "MOJI REKORDI" || title == "PROFIL" || title == "POSTIGNUĆA" || title == "KOLEKCIJA"
        stack.spacing=title == "BOPAVI" || compact ? 7 : 12
        stack.translatesAutoresizingMaskIntoConstraints=false
        stack.layoutMargins=title == "BOPAVI" ?
            UIEdgeInsets(top:12,left:18,bottom:12,right:18) :
            compact ? UIEdgeInsets(top:7,left:12,bottom:8,right:12) :
            UIEdgeInsets(top:18,left:18,bottom:28,right:18)
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
            logo.heightAnchor.constraint(equalToConstant:compact ? 68 : 94).isActive=true
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
            // Retain an independently accessible native heading rather than
            // a baked title inside the concept painting. Saturated azure
            // metal and cyan edge mirror Android's shared header treatment.
            headline.backgroundColor=UIColor(red:0.025,green:0.28,blue:0.66,alpha:0.98)
            headline.layer.cornerRadius=23
            headline.layer.borderWidth=2
            headline.layer.borderColor=UIColor(red:0.45,green:0.91,blue:1,alpha:1).cgColor
            headline.layer.shadowColor=UIColor(red:0.02,green:0.78,blue:1,alpha:1).cgColor
            headline.layer.shadowOpacity=0.24
            headline.layer.shadowRadius=6
            headline.layer.shadowOffset=CGSize(width:0,height:2)
            headline.clipsToBounds=false
            headline.heightAnchor.constraint(greaterThanOrEqualToConstant:compact ? 49 : 58).isActive=true
            if !subtitle.isEmpty {label(subtitle,15,UIColor(red:0.79,green:0.93,blue:1,alpha:1),stack)}
        }
        return stack
    }
    override func viewDidLayoutSubviews(){
        super.viewDidLayoutSubviews()
        backgroundGradient?.frame=view.bounds
        // Apply the same non-scrolling safe-area fit to menus, pause and result.
        // Each remains a real UIKit interface with functioning native buttons.
        func fit(_ scroll:UIScrollView,_ panel:UIStackView) {
            guard !scroll.isScrollEnabled, scroll.bounds.height>0,
                  panel.bounds.height>0 else{return}
            let naturalHeight=panel.bounds.height
            let factor=min(CGFloat(1),scroll.bounds.height/naturalHeight)
            let shift = -(naturalHeight * (1-factor) / 2)
            let desired=CGAffineTransform(scaleX:factor,y:factor)
                .translatedBy(x:0,y:factor>0 ? shift/factor : 0)
            if panel.transform != desired {panel.transform=desired}
        }
        if let scroll=view.subviews.compactMap({$0 as? UIScrollView}).first,
           let stack=scroll.subviews.compactMap({$0 as? UIStackView}).first {
            fit(scroll,stack)
        }
        if let scroll=pauseFitViewport,let panel=pauseFitPanel {
            fit(scroll,panel)
        }
        if let scroll=resultFitViewport,let panel=resultFitPanel {
            fit(scroll,panel)
        }
    }
    @discardableResult private func label(_ text:String,_ size:CGFloat,_ color:UIColor,_ into:UIStackView)->UILabel {
        let l=UILabel()
        l.numberOfLines=0;l.textAlignment = .center;l.text=ui(text)
        let font=UIFont.systemFont(ofSize:size,weight:size>=26 ? .black : .semibold)
        l.font=UIFont(descriptor:font.fontDescriptor.withDesign(.rounded) ?? font.fontDescriptor,size:size)
        l.adjustsFontForContentSizeCategory=true
        l.textColor=color
        l.setContentCompressionResistancePriority(.required,for:.vertical);into.addArrangedSubview(l)
        return l
    }
    private func sectionHeading(_ title:String,in stack:UIStackView) {
        let heading=label("   "+title,15,.white,stack)
        heading.textAlignment = .left
        let sectionFont=UIFont.systemFont(ofSize:15,weight:.heavy)
        heading.font=UIFont(descriptor:sectionFont.fontDescriptor.withDesign(.rounded)
            ?? sectionFont.fontDescriptor,size:15)
        heading.backgroundColor=UIColor(red:0.03,green:0.16,blue:0.36,alpha:0.97)
        heading.layer.cornerRadius=20
        heading.layer.borderWidth=1
        heading.layer.borderColor=UIColor(red:0.25,green:0.72,blue:1,alpha:0.65).cgColor
        heading.clipsToBounds=true
        heading.heightAnchor.constraint(greaterThanOrEqualToConstant:44).isActive=true
    }
    /// A single 50pt upper-left Back control, not a full-width footer button.
    private func menuBack(_ stack:UIStackView,action:@escaping()->Void) {
        let row=UIView()
        row.heightAnchor.constraint(equalToConstant:53).isActive=true
        let arrow=UIButton(type:.system)
        arrow.translatesAutoresizingMaskIntoConstraints=false
        arrow.setTitle("←",for:.normal)
        arrow.setTitleColor(.white,for:.normal)
        arrow.titleLabel?.font=UIFont.systemFont(ofSize:30,weight:.heavy)
        arrow.backgroundColor=UIColor(red:0.035,green:0.40,blue:0.84,alpha:1)
        arrow.layer.cornerRadius=25
        arrow.layer.borderWidth=2
        arrow.layer.borderColor=UIColor(red:0.75,green:0.97,blue:1,alpha:1).cgColor
        arrow.accessibilityLabel=progress.languageCode=="hr" ? "Natrag" : "Back"
        arrow.accessibilityTraits.insert(.button)
        arrow.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        row.addSubview(arrow)
        NSLayoutConstraint.activate([
            arrow.leadingAnchor.constraint(equalTo:row.leadingAnchor),
            arrow.topAnchor.constraint(equalTo:row.topAnchor,constant:1),
            arrow.widthAnchor.constraint(equalToConstant:50),
            arrow.heightAnchor.constraint(equalToConstant:50)
        ])
        stack.insertArrangedSubview(row,at:0)
    }
    private func button(_ title:String,in stack:UIStackView,primary:Bool=true,muted:Bool=false,action:@escaping()->Void){
        let b=BopaviActionButton(primary:primary,muted:muted)
        b.setTitle(ui(title),for:.normal)
        b.accessibilityLabel=ui(title)
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
        tile.accessibilityLabel=ui("\(BopaviCore.names[world]), otključano, " +
            "level \(progress.streamFrontier(world)), prikupljeno \(progress.collectibles(world)), " +
            "rekord \(progress.best(world)), zvjezdice \(progress.worldStars(world)) od 30" +
            (selected ? ", odabrano" : "") +
            ", upravljanje dodirom")
        tile.heightAnchor.constraint(equalToConstant:144).isActive=true
        tile.accessibilityHint=ui("Otvori levele svijeta")
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
        // The full illustrated tile is the accessible tap target. Do not
        // obscure world art with a redundant decorative TAP badge.
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
        headline.text=ui(BopaviCore.names[world]).uppercased()
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
            startSceneryDrift(backdrop)
            view.insertSubview(backdrop,at:0)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
        }
        // HomeArt contains scenery alone; the native wordmark, selected pilot,
        // currency and all controls sit above it as responsive live views.
        // Home fits exactly within the available iPhone safe area: no scrolling,
        // clipped footer, or large blank region below the navigation.
        if let homeViewport=view.subviews.compactMap({$0 as? UIScrollView}).first {
            // Keep the footer at the bottom on tall devices, but permit a
            // larger intrinsic stack on short devices so viewport fitting
            // can scale all hit targets coherently instead of clipping them.
            stack.heightAnchor.constraint(greaterThanOrEqualTo:homeViewport.frameLayoutGuide.heightAnchor).isActive=true
        }
        // The two original transparent hero assets remain independent.
        let record=UILabel()
        record.text="★  "+(progress.languageCode=="hr" ? "NAJBOLJI REZULTAT: " : "BEST SCORE: ")+String(progress.bestPoints())
        record.font=UIFont.systemFont(ofSize:17,weight:.heavy)
        record.textColor = .white
        record.textAlignment = .center
        record.backgroundColor=UIColor(red:0.02,green:0.17,blue:0.42,alpha:0.93)
        record.layer.cornerRadius=21;record.clipsToBounds=true
        record.layer.borderWidth=2
        record.layer.borderColor=UIColor(red:0.26,green:0.84,blue:1,alpha:1).cgColor
        record.heightAnchor.constraint(equalToConstant:47).isActive=true
        record.accessibilityLabel=record.text
        stack.addArrangedSubview(record)
        let homeLogo=UIImageView(image:UIImage(named:"Logo"))
        homeLogo.contentMode = .scaleAspectFit
        homeLogo.isAccessibilityElement=false
        homeLogo.heightAnchor.constraint(equalToConstant:154).isActive=true
        stack.addArrangedSubview(homeLogo)
        let homePilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        homePilot.contentMode = .scaleAspectFit
        homePilot.isAccessibilityElement=false
        homePilot.heightAnchor.constraint(equalToConstant:310).isActive=true
        stack.addArrangedSubview(homePilot)
        let spacer=UIView()
        spacer.setContentHuggingPriority(.defaultLow,for:.vertical)
        stack.addArrangedSubview(spacer)
        button("▶  IGRAJ",in:stack){
            self.startGame(self.progress.chosenWorld(),1)
        }
        label(progress.languageCode=="hr" ? "Dodirni zaslon za zamah. Leti što dalje!" :
            "Tap to flap. Fly as far as you can!",14,.white,stack)
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
        menuBack(stack){self.showHome()}
    }
    private func showLevels(_ world:Int,page:Int){
        gameWorld=world;progress.chooseWorld(world)
        let s=menu("LEVELI","Odaberite level · \(BopaviCore.names[world]) · " +
            "TAP ZA ZAMAH")
        let maxNumber=min(BopaviCore.levelsPerWorld,progress.frontier(world))
        let safePage=max(1,min(page,BopaviCore.levelsPerWorld))
        let start=((safePage-1)/20)*20+1
        let zone=BopaviCore.create(world,start).zone
        let pageNumber=(start-1)/20+1
        let pageCount=(BopaviCore.levelsPerWorld-1)/20+1
        // The 5x4 illustrated grid and Back button remain on screen.
        let pageCaption="ZONA \(zone) · STRANICA \(pageNumber) / \(pageCount)" +
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
        // All 20 illustrated tiles are genuine UIKit controls, with the
        // existing level unlocks, accessible labels and saved star ratings.
        // Avoid duplicating the level picker, Continue and long instructions
        // below the paged gallery: they reduce its size on compact iPhones.
        menuBack(s){self.showWorlds()}
    }
    /// Every image is a reusable, real world illustration. Completion is read
    /// exclusively from saved frontier; no invented three-star achievements.
    private func levelTile(_ world:Int,_ n:Int,frontier:Int,kind:Int,in row:UIStackView){
        let unlocked=n<=frontier
        let completed=n<frontier
        let selected=n==frontier
        let cell=BopaviActionButton(primary:false)
        cell.setTitle("",for:.normal)
        cell.accessibilityLabel=ui("Level \(n), \(LevelKind.name(kind)), \(!unlocked ? "zaključan" : (completed ? "dovršen" : "otključan")), " +
            "zvjezdice \(progress.bestLevelStars(world,n)) od 3")
        cell.accessibilityHint=ui(unlocked ? "Pokreni level" : "Prvo dovrši prethodni level")
        cell.layer.cornerRadius=16
        cell.layer.borderWidth=selected ? 3 : 1
        cell.layer.borderColor=(selected
            ? UIColor(red:1,green:0.78,blue:0.30,alpha:1)
            : UIColor(red:0.43,green:0.84,blue:1,alpha:0.9)).cgColor
        cell.clipsToBounds=true
        cell.heightAnchor.constraint(equalToConstant:96).isActive=true
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
    /// A genuine 3×3 UIKit gallery using the nine shipped pilot sprites.
    /// All purchases use earned offline coins and an explicit confirmation;
    /// no cloud account, store billing, placeholder diamonds or fake pilots.
    private func showPilotPicker(_ world:Int,_ number:Int) {
        gameWorld=world;gameNumber=number
        let croatian=progress.languageCode=="hr"
        let selected=progress.skinIndex()
        let stack=menu(croatian ? "ODABERI LIKA" : "CHOOSE YOUR PILOT",
                       croatian ? "SVAKI PILOT IMA SVOJU PRIČU" : "EVERY PILOT HAS A STORY")
        let head=UIStackView()
        head.axis = .horizontal
        head.alignment = .center
        head.spacing=8
        head.isLayoutMarginsRelativeArrangement=true
        head.layoutMargins=UIEdgeInsets(top:4,left:3,bottom:4,right:3)
        head.backgroundColor = .clear
        head.heightAnchor.constraint(equalToConstant:230).isActive=true
        stack.addArrangedSubview(head)
        let chosenPicture=UIImageView(image:UIImage(named:"BopiPortrait\(selected)"))
        chosenPicture.contentMode = .scaleAspectFit
        chosenPicture.isAccessibilityElement=false
        chosenPicture.widthAnchor.constraint(equalToConstant:203).isActive=true
        chosenPicture.heightAnchor.constraint(equalToConstant:222).isActive=true
        head.addArrangedSubview(chosenPicture)
        let pilotSummary=UIStackView()
        pilotSummary.axis = .vertical;pilotSummary.alignment = .fill
        pilotSummary.spacing=5
        pilotSummary.isLayoutMarginsRelativeArrangement=true
        pilotSummary.layoutMargins=UIEdgeInsets(top:14,left:7,bottom:14,right:7)
        pilotSummary.backgroundColor=UIColor(red:0.035,green:0.19,blue:0.42,alpha:0.96)
        pilotSummary.layer.cornerRadius=21
        pilotSummary.layer.borderWidth=2
        pilotSummary.layer.borderColor=UIColor(red:0.25,green:0.85,blue:1,alpha:1).cgColor
        head.addArrangedSubview(pilotSummary)
        let currentName=UILabel()
        currentName.text="♛  "+progress.skinNames[selected].uppercased()
        currentName.numberOfLines=1
        let nameFont=UIFont.systemFont(ofSize:19,weight:.black)
        currentName.font=UIFont(descriptor:nameFont.fontDescriptor.withDesign(.rounded)
            ?? nameFont.fontDescriptor,size:19)
        currentName.adjustsFontSizeToFitWidth=true
        currentName.minimumScaleFactor=0.7
        currentName.textColor = .white
        currentName.textAlignment = .center
        pilotSummary.addArrangedSubview(currentName)
        let chosenDescription=UILabel()
        chosenDescription.text=croatian ? "ODABRANI PILOT" : "SELECTED PILOT"
        chosenDescription.font=UIFont.systemFont(ofSize:11,weight:.bold)
        chosenDescription.textColor=UIColor(red:0.80,green:0.92,blue:1,alpha:1)
        chosenDescription.textAlignment = .center
        pilotSummary.addArrangedSubview(chosenDescription)
        let pilotRecord=UILabel()
        let ownedCount=(0...8).filter{progress.owned($0)}.count
        pilotRecord.text="★  \(progress.worldStars(world))/30   ·   \(ownedCount)/9"
        pilotRecord.font=UIFont.monospacedDigitSystemFont(ofSize:12,weight:.bold)
        pilotRecord.textColor=UIColor(red:1,green:0.83,blue:0.3,alpha:1)
        pilotRecord.textAlignment = .center
        pilotRecord.adjustsFontSizeToFitWidth=true
        pilotRecord.minimumScaleFactor=0.7
        pilotRecord.accessibilityLabel=(croatian ? "Zvjezdice i otključani piloti" :
            "World stars and unlocked pilots") + ": \(progress.worldStars(world))/30, \(ownedCount)/9"
        pilotSummary.addArrangedSubview(pilotRecord)
        for rowIndex in 0..<3 {
            let galleryRow=UIStackView()
            galleryRow.axis = .horizontal
            galleryRow.spacing=6
            galleryRow.distribution = .fillEqually
            stack.addArrangedSubview(galleryRow)
            for column in 0..<3 {
                let index=rowIndex*3+column
                let owned=progress.owned(index)
                let active=index==selected
                let card=UIButton(type:.custom)
                card.layer.cornerRadius=18
                card.layer.borderWidth=active ? 3 : 2
                card.layer.borderColor=(active
                    ? UIColor(red:1,green:0.77,blue:0.23,alpha:1)
                    : UIColor(red:0.25,green:0.68,blue:0.97,alpha:1)).cgColor
                card.backgroundColor=UIColor(red:0.025,green:active ? 0.29 : 0.19,
                                            blue:active ? 0.63 : 0.42,alpha:0.98)
                card.clipsToBounds=true
                card.heightAnchor.constraint(equalToConstant:118).isActive=true
                let pilotState:String
                if active {
                    pilotState=croatian ? "odabran" : "selected"
                } else if owned {
                    pilotState=croatian ? "dostupan" : "available"
                } else {
                    pilotState="\(croatian ? "otključaj za" : "unlock for") \(progress.costs[index]) \(croatian ? "kovanica" : "coins")"
                }
                card.accessibilityLabel="\(progress.skinNames[index]), \(pilotState)"
                if active {card.accessibilityTraits.insert(.selected)}
                let image=UIImageView(image:UIImage(named:"BopiPortrait\(index)"))
                image.translatesAutoresizingMaskIntoConstraints=false
                image.contentMode = .scaleAspectFit
                image.alpha=owned ? 1 : 0.80
                image.isUserInteractionEnabled=false
                image.accessibilityElementsHidden=true
                card.addSubview(image)
                let caption=UILabel()
                caption.translatesAutoresizingMaskIntoConstraints=false
                let pilotCaption=active ? "✓" : (owned
                    ? (croatian ? "DOSTUPAN" : "READY")
                    : "● \(progress.costs[index])")
                caption.text="\(progress.skinNames[index])\n\(pilotCaption)"
                caption.numberOfLines=2
                caption.textAlignment = .center
                caption.textColor=active ? UIColor(red:1,green:0.84,blue:0.33,alpha:1) : .white
                caption.font=UIFont.systemFont(ofSize:11,weight:.heavy)
                caption.adjustsFontSizeToFitWidth=true
                caption.minimumScaleFactor=0.78
                caption.isUserInteractionEnabled=false
                card.addSubview(caption)
                NSLayoutConstraint.activate([
                    image.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:3),
                    image.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-3),
                    image.topAnchor.constraint(equalTo:card.topAnchor,constant:2),
                    image.heightAnchor.constraint(equalToConstant:80),
                    caption.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:3),
                    caption.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-3),
                    caption.bottomAnchor.constraint(equalTo:card.bottomAnchor,constant:-3),
                    caption.heightAnchor.constraint(equalToConstant:38)
                ])
                card.addAction(UIAction{[weak self] _ in
                    guard let self=self else{return}
                    self.sound.effect("click")
                    if self.progress.owned(index) {
                        _=self.progress.selectOrBuy(index)
                        self.showPilotPicker(world,number)
                    } else {
                        let price=self.progress.costs[index]
                        let prompt=UIAlertController(
                            title:self.progress.skinNames[index],
                            message:croatian
                                ? "Otključati za \(price) kovanica? Stanje: \(self.progress.coins())."
                                : "Unlock for \(price) earned coins? Wallet: \(self.progress.coins()).",
                            preferredStyle:.alert)
                        prompt.addAction(UIAlertAction(title:croatian ? "ODUSTANI" : "CANCEL",
                                                       style:.cancel))
                        prompt.addAction(UIAlertAction(title:croatian ? "OTKLJUČAJ" : "UNLOCK",
                                                       style:.default){_ in
                            if self.progress.selectOrBuy(index) {
                                self.sound.effect("purchase")
                                self.showPilotPicker(world,number)
                            } else {
                                self.alert(croatian ? "Nedovoljno kovanica" : "Not enough coins",
                                           croatian ? "Skupljaj kovanice tijekom leta." :
                                                      "Collect more coins while flying.")
                            }
                        })
                        self.present(prompt,animated:true)
                    }
                },for:.touchUpInside)
                galleryRow.addArrangedSubview(card)
            }
        }
        let flightTitle="\(croatian ? "▶  POLETI S" : "▶  FLY WITH") \(progress.skinNames[selected].uppercased())"
        button(flightTitle,in:stack) {
            self.startGame(world,number)
        }
        menuBack(stack){self.showLevels(world,page:number)}
    }

    /// All elements are real UIKit views and actions. A full-screen dimmer
    /// intercepts touches while physics remains frozen; only Continue resumes.
    private func showPremiumPause(for canvas:GameCanvas){
        let overlay=UIView()
        overlay.translatesAutoresizingMaskIntoConstraints=false
        overlay.backgroundColor=UIColor(red:0,green:0.055,blue:0.14,alpha:0.70)
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
        scroll.alwaysBounceVertical=false
        scroll.isScrollEnabled=false
        pauseFitViewport=scroll
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
        pauseFitPanel=panel
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
        mascot.heightAnchor.constraint(equalToConstant:154).isActive=true
        panel.addArrangedSubview(mascot)
        label(progress.languageCode=="hr" ? "♛  PAUZA" : "♛  PAUSE",33,UIColor.white,panel)
        label((progress.languageCode=="hr" ? "Let je zaustavljen" : "Flight paused"),14,
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
            control.setTitle(self.ui(caption),for:.normal)
            control.setTitleColor(.white,for:.normal)
            control.titleLabel?.font=UIFont.systemFont(ofSize:18,weight:.heavy)
            control.titleLabel?.adjustsFontSizeToFitWidth=true
            control.titleLabel?.minimumScaleFactor=0.7
            control.accessibilityLabel=self.ui(caption)
            control.heightAnchor.constraint(equalToConstant:59).isActive=true
            control.addAction(UIAction{_ in self.sound.effect("click");handler()},for:.touchUpInside)
            panel.addArrangedSubview(control)
        }
        command(progress.languageCode=="hr" ? "▶  NASTAVI" : "▶  CONTINUE",style:0){close(true)}
        command(progress.languageCode=="hr" ? "↻  PONOVO" : "↻  RETRY",style:1){
            close(false);self.startGame(self.gameWorld,1)
        }
        command(progress.languageCode=="hr" ? "↪  IZLAZ" : "↪  EXIT",style:2){close(false);self.showHome()}
        label(progress.languageCode=="hr" ?
            "Nastavi let, pokušaj ponovno ili se vrati na početnu." :
            "Resume your flight, retry or return Home.",13,
            UIColor(red:0.82,green:0.92,blue:1,alpha:1),panel)
        UIAccessibility.post(notification:.screenChanged,argument:panel)
    }
    private func startGame(_ world:Int,_ number:Int){
        clear();gameWorld=world;gameNumber=number
        let game=GameSimulation(BopaviCore.createStream(world,number),endless:true,initialShield:0,initialMagnet:0,difficulty:progress.difficulty,initialOrdinal:number)
        sound.startWorld(world)
        let gameCanvas=GameCanvas(game:game,reducedMotion:progress.lessMotion,skinIndex:progress.skinIndex(),
            coinWallet:{[weak self] in self?.progress.coins() ?? 0},languageCode:progress.languageCode)
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
    /// Endless flight result. Keep the user's older records but no shop/world routes.
    private func showResult(_ g:GameSimulation){
        let previous=progress.bestPoints()
        let newRecord=g.score()>previous && g.score()>0
        sound.effect("hit")
        progress.recordRun(g)
        clear()
        view.backgroundColor=UIColor(red:0.04,green:0.25,blue:0.54,alpha:1)
        let backdrop=UIImageView(image:UIImage(named:"HomeArt"))
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
        shade.translatesAutoresizingMaskIntoConstraints=false
        shade.backgroundColor=UIColor(red:0.01,green:0.07,blue:0.18,alpha:0.51)
        view.addSubview(shade)
        NSLayoutConstraint.activate([
            shade.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            shade.trailingAnchor.constraint(equalTo:view.trailingAnchor),
            shade.topAnchor.constraint(equalTo:view.topAnchor),
            shade.bottomAnchor.constraint(equalTo:view.bottomAnchor)
        ])
        let scroll=UIScrollView()
        scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.isScrollEnabled=false
        scroll.showsVerticalScrollIndicator=false
        resultFitViewport=scroll
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.safeAreaLayoutGuide.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:view.safeAreaLayoutGuide.trailingAnchor)
        ])
        let panel=UIStackView()
        panel.axis = .vertical
        panel.alignment = .fill
        panel.spacing=12
        panel.translatesAutoresizingMaskIntoConstraints=false
        panel.isLayoutMarginsRelativeArrangement=true
        panel.layoutMargins=UIEdgeInsets(top:18,left:18,bottom:18,right:18)
        resultFitPanel=panel
        scroll.addSubview(panel)
        NSLayoutConstraint.activate([
            panel.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            panel.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            panel.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            panel.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            panel.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor),
            panel.heightAnchor.constraint(greaterThanOrEqualTo:scroll.frameLayoutGuide.heightAnchor)
        ])
        let portrait=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        portrait.contentMode = .scaleAspectFit
        portrait.isAccessibilityElement=false
        portrait.heightAnchor.constraint(equalToConstant:198).isActive=true
        panel.addArrangedSubview(portrait)
        label(newRecord ? "♛  NOVI REKORD!" : "REZULTAT",32,.white,panel)
        let stats=UIStackView()
        stats.axis = .vertical
        stats.alignment = .fill
        stats.spacing=7
        stats.layoutMargins=UIEdgeInsets(top:14,left:15,bottom:14,right:15)
        stats.isLayoutMarginsRelativeArrangement=true
        stats.layer.cornerRadius=24
        stats.layer.borderWidth=3
        stats.layer.borderColor=UIColor(red:0.3,green:0.85,blue:1,alpha:1).cgColor
        stats.backgroundColor=UIColor(red:0.02,green:0.13,blue:0.32,alpha:0.96)
        panel.addArrangedSubview(stats)
        func stat(_ title:String,_ number:Int){
            let row=UIStackView()
            row.axis = .horizontal
            row.alignment = .center
            let heading=UILabel()
            heading.text=title
            heading.font=UIFont.systemFont(ofSize:16,weight:.bold)
            heading.textColor = .white
            heading.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
            row.addArrangedSubview(heading)
            let value=UILabel()
            value.text=String(number)
            value.font=UIFont.systemFont(ofSize:24,weight:.heavy)
            value.textColor=UIColor(red:1,green:0.81,blue:0.3,alpha:1)
            row.addArrangedSubview(value)
            row.heightAnchor.constraint(equalToConstant:54).isActive=true
            stats.addArrangedSubview(row)
        }
        stat("★  REZULTAT",g.score())
        stat("♛  PROLAZI",g.totalPassed)
        stat("🏆  NAJBOLJI REZULTAT",progress.bestPoints())
        button("▶  PONOVO",in:panel){self.startGame(self.gameWorld,1)}
        button("⌂  POČETNA",in:panel,primary:false){self.showHome()}
        UIAccessibility.post(notification:.screenChanged,argument:panel)
    }

    private func showPerks(){showPerks(.settings)}
    private func showPerks(_ origin:MenuOrigin){
        let croatian=progress.languageCode=="hr"
        let s=menu("TRGOVINA",croatian ? "Oprema za nove avanture" : "Gear for the next adventure")
        menuBack(s){if origin == .home {self.showHome()} else {self.showSettings()}}
        // Full-width illustrated wallet: ONLY actual saved crown currency.
        // No invented gems, IAP buttons, advertisements, or fake purchases.
        let wallet=UIStackView()
        wallet.axis = .horizontal;wallet.alignment = .center;wallet.spacing=7
        wallet.isLayoutMarginsRelativeArrangement=true
        wallet.layoutMargins=UIEdgeInsets(top:7,left:12,bottom:7,right:12)
        wallet.backgroundColor=UIColor(red:0.03,green:0.16,blue:0.38,alpha:0.98)
        wallet.layer.cornerRadius=22;wallet.layer.borderWidth=2
        wallet.layer.borderColor=UIColor(red:1,green:0.79,blue:0.30,alpha:1).cgColor
        wallet.heightAnchor.constraint(equalToConstant:69).isActive=true
        wallet.isAccessibilityElement=true
        wallet.accessibilityLabel=(croatian ? "Osvojene kovanice: " : "Earned coins: ")+String(progress.coins())
        let crown=UILabel()
        crown.text="♛";crown.font=UIFont.systemFont(ofSize:35,weight:.heavy)
        crown.textColor=UIColor(red:1,green:0.81,blue:0.30,alpha:1)
        crown.textAlignment = .center
        crown.widthAnchor.constraint(equalToConstant:49).isActive=true
        wallet.addArrangedSubview(crown)
        let balance=UIStackView()
        balance.axis = .vertical;balance.alignment = .fill;balance.spacing=2
        let amount=label(String(progress.coins())+(croatian ? " KOVANICA" : " COINS"),
            22,UIColor(red:1,green:0.81,blue:0.30,alpha:1),balance)
        amount.textAlignment = .left;amount.numberOfLines=1
        amount.adjustsFontSizeToFitWidth=true;amount.minimumScaleFactor=0.7
        let detail=label(croatian ? "Za kovanice osvojene igrom — bez stvarnog novca." :
            "Earned in-game · no real money",11,
            UIColor(red:0.81,green:0.93,blue:1,alpha:1),balance)
        detail.textAlignment = .left;detail.numberOfLines=1
        detail.adjustsFontSizeToFitWidth=true;detail.minimumScaleFactor=0.7
        wallet.addArrangedSubview(balance)
        s.addArrangedSubview(wallet)
        let bonus=progress.bonusCoinsAvailable()
        if bonus>0 {
            button(croatian ? "🎁  PREUZMI \(bonus) KOVANICA ZA BODOVE" :
                   "🎁  CLAIM \(bonus) SCORE REWARD COINS",in:s,primary:false){
                if self.progress.claimBonusCoins()>0 {
                    self.sound.effect("purchase");self.showPerks(origin)
                }
            }
        }
        sectionHeading(croatian ? "OPREMA ZA BOPIJEV LET" : "FLIGHT EQUIPMENT",in:s)
        let names=croatian
            ? ["ŠTIT","MAGNET","DVOSTRUKI ŠTIT","DUGI MAGNET","TROSTRUKI ŠTIT","SUPER MAGNET"]
            : ["SHIELD","MAGNET","DOUBLE SHIELD","LONG MAGNET","TRIPLE SHIELD","SUPER MAGNET"]
        let explanations=croatian
            ? ["Upija jedan sudar.","Privlači 8 sekundi.","Upija dva sudara.",
               "Privlači 12 sekundi.","Upija tri sudara.","Privlači 18 sekundi."]
            : ["Absorbs one collision.","Attracts coins for 8 seconds.","Absorbs two collisions.",
               "Attracts coins for 12 seconds.","Absorbs three collisions.","Attracts coins for 18 seconds."]
        let cardGrid=UIStackView()
        cardGrid.axis = .vertical;cardGrid.spacing=6
        for row in 0..<3 {
            let cardRow=UIStackView()
            cardRow.axis = .horizontal;cardRow.alignment = .fill
            cardRow.distribution = .fillEqually;cardRow.spacing=6
            for n in (row*2)..<(row*2+2) {
                let price=progress.perkPrices[n]
                let owned=progress.perkCount(n)
                // One 97-point accessible control per offer, matching Android:
                // there is no oversized nested UIButton to shrink the whole UI.
                let card=UIButton(type:.custom)
                card.backgroundColor=UIColor(red:0.04,green:0.17,blue:0.38,alpha:0.99)
                card.layer.cornerRadius=18;card.layer.borderWidth=2
                card.layer.borderColor=(n%2==0 ?
                    UIColor(red:1,green:0.81,blue:0.30,alpha:1) :
                    UIColor(red:0.17,green:0.84,blue:1,alpha:1)).cgColor
                card.heightAnchor.constraint(equalToConstant:97).isActive=true
                let orderLabel=croatian ? "KUPI " : "BUY "
                let unitLabel=croatian ? " kovanica. U torbi: " : " coins. Owned: "
                card.accessibilityLabel="\(orderLabel)\(names[n]). \(explanations[n]) \(price)\(unitLabel)\(owned)"
                let layout=UIStackView()
                layout.axis = .vertical;layout.alignment = .fill;layout.spacing=4
                layout.isUserInteractionEnabled=false
                layout.translatesAutoresizingMaskIntoConstraints=false
                card.addSubview(layout)
                NSLayoutConstraint.activate([
                    layout.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:7),
                    layout.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-7),
                    layout.topAnchor.constraint(equalTo:card.topAnchor,constant:7),
                    layout.bottomAnchor.constraint(equalTo:card.bottomAnchor,constant:-7)
                ])
                let top=UIStackView()
                top.axis = .horizontal;top.alignment = .center;top.spacing=3
                let icon=UILabel()
                icon.text=n%2==0 ? "🛡" : "🧲"
                icon.font=UIFont.systemFont(ofSize:26)
                icon.textAlignment = .center
                icon.widthAnchor.constraint(equalToConstant:38).isActive=true
                top.addArrangedSubview(icon)
                let caption=UIStackView()
                caption.axis = .vertical;caption.alignment = .fill;caption.spacing=0
                let title=label(names[n],13,.white,caption)
                title.textAlignment = .left;title.numberOfLines=1
                title.adjustsFontSizeToFitWidth=true;title.minimumScaleFactor=0.64
                let description=label(explanations[n],10,
                    UIColor(red:0.79,green:0.93,blue:1,alpha:1),caption)
                description.textAlignment = .left;description.numberOfLines=1
                description.adjustsFontSizeToFitWidth=true;description.minimumScaleFactor=0.7
                top.addArrangedSubview(caption)
                layout.addArrangedSubview(top)
                let foot=UIStackView()
                foot.axis = .horizontal;foot.alignment = .center;foot.spacing=4
                let priceLabel=UILabel()
                priceLabel.text="♛ \(price) · "+(croatian ? "U torbi: " : "Owned: ")+String(owned)
                priceLabel.font=UIFont.systemFont(ofSize:11,weight:.bold)
                priceLabel.textColor=UIColor(red:1,green:0.82,blue:0.32,alpha:1)
                priceLabel.adjustsFontSizeToFitWidth=true;priceLabel.minimumScaleFactor=0.62
                foot.addArrangedSubview(priceLabel)
                let buy=UILabel()
                buy.text=croatian ? "KUPI ›" : "BUY ›"
                buy.font=UIFont.systemFont(ofSize:11,weight:.heavy)
                buy.textAlignment = .center;buy.textColor = .white
                buy.backgroundColor=UIColor(red:0.02,green:0.49,blue:0.90,alpha:1)
                buy.layer.cornerRadius=12;buy.clipsToBounds=true
                buy.layer.borderWidth=1
                buy.layer.borderColor=UIColor(red:0.43,green:0.91,blue:1,alpha:1).cgColor
                buy.widthAnchor.constraint(equalToConstant:54).isActive=true
                buy.heightAnchor.constraint(equalToConstant:29).isActive=true
                foot.addArrangedSubview(buy)
                layout.addArrangedSubview(foot)
                card.addAction(UIAction{_ in
                    if self.progress.buyPerk(n) {
                        self.sound.effect("purchase");self.showPerks(origin)
                    } else {
                        self.alert(croatian ? "Kupnja nije moguća" : "Purchase unavailable",
                                   croatian ? "Nedovoljno kovanica ili je zaliha puna." :
                                              "Not enough coins or inventory is full.")
                    }
                },for:.touchUpInside)
                cardRow.addArrangedSubview(card)
            }
            cardGrid.addArrangedSubview(cardRow)
        }
        s.addArrangedSubview(cardGrid)
        label(croatian ? "Kupljena oprema automatski se koristi na početku sljedećeg leta." :
              "Purchased equipment automatically activates when your next flight begins.",12,.white,s)
        label(croatian ? "Za svakih novih 1.000 bodova najboljeg rezultata dobivaš 1 kovanicu." :
              "Earn one coin for each new 1,000 points of your personal best.",12,.white,s)
        label(croatian ? "Odaberi jednog od devet pilota prije leta." :
              "Choose from nine pilots before takeoff.",12,.white,s)
    }
    /// All profile values are derived from local save state; no fabricated account.
    private func showProfile(){
        let s=menu("PROFIL","Tvoj letački dnevnik i osobni rezultati")
        // All metrics, badges and world ranks are derived from offline saves.
        // Reuse the same upper-left native Back control used on all menus.
        menuBack(s){self.showHome()}
        // Profile is the saved actual pilot, not an immutable Bopi banner.
        let skin=progress.skinIndex()
        let hero=UIStackView()
        hero.axis = .horizontal;hero.alignment = .center;hero.spacing=8
        hero.layoutMargins=UIEdgeInsets(top:6,left:7,bottom:6,right:7)
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
        portraitBox.accessibilityLabel=ui("Letački profil, ") + progress.playerName + (progress.languageCode=="hr" ? ", lik " : ", pilot ") + progress.skinNames[skin]
        portraitBox.widthAnchor.constraint(equalToConstant:124).isActive=true
        let portrait=UIImageView(image:UIImage(named:"BopiPortrait"+String(skin)))
        portrait.contentMode = .scaleAspectFit
        portrait.heightAnchor.constraint(equalToConstant:105).isActive=true
        portrait.isAccessibilityElement=false
        portraitBox.addArrangedSubview(portrait)
        let levelLabel=label("LEVEL "+String(progress.streamFrontier(progress.chosenWorld())),
            11,UIColor(red:1,green:0.77,blue:0.30,alpha:1),portraitBox)
        levelLabel.heightAnchor.constraint(equalToConstant:20).isActive=true
        // Gauge the selected world's *saved* stars. No invented XP, metres
        // or account level: it is a real 0...30 world completion indicator.
        let profileWorld=progress.chosenWorld()
        let profileStars=max(0,min(30,progress.worldStars(profileWorld)))
        let starGauge=UIProgressView(progressViewStyle:.default)
        starGauge.progress=Float(profileStars)/30
        starGauge.progressTintColor=UIColor(red:1,green:0.76,blue:0.30,alpha:1)
        starGauge.trackTintColor=UIColor(red:0.06,green:0.24,blue:0.49,alpha:1)
        starGauge.transform=CGAffineTransform(scaleX:1,y:3)
        starGauge.heightAnchor.constraint(equalToConstant:8).isActive=true
        starGauge.isAccessibilityElement=true
        starGauge.accessibilityLabel=(progress.languageCode=="hr" ? "Zvjezdice svijeta " : "Stars in world ") + ui(BopaviCore.names[profileWorld])
        starGauge.accessibilityValue="\(profileStars)/30"
        portraitBox.addArrangedSubview(starGauge)
        let starCaption=label("★  \(profileStars)/30 · "+ui(BopaviCore.names[profileWorld]),10,.white,portraitBox)
        starCaption.numberOfLines=1
        starCaption.adjustsFontSizeToFitWidth=true;starCaption.minimumScaleFactor=0.8
        starCaption.heightAnchor.constraint(equalToConstant:18).isActive=true
        hero.addArrangedSubview(portraitBox)
        let stats=UIStackView()
        stats.axis = .vertical;stats.alignment = .fill;stats.spacing=3
        let pilotName=label(ui(progress.playerName.uppercased()),20,.white,stats)
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
            tile.accessibilityLabel=ui(caption)+": "+value
            let textLabel=UILabel()
            textLabel.text=ui(caption);textLabel.textColor = .white
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
            tile.heightAnchor.constraint(equalToConstant:27).isActive=true
            stats.addArrangedSubview(tile)
        }
        hero.addArrangedSubview(stats)
        s.addArrangedSubview(hero)
        // Saved-world identity is already in the live star gauge.
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
            tile.accessibilityLabel=ui(name) + (progress.languageCode=="hr" ? ", cilj " : ", goal ") + ui(requirement) + ", " + ui(earned ? "osvojeno" : "zaključano")
            let glyph=label(earned ? icon:"🔒",24,
                UIColor(red:1,green:0.77,blue:0.30,alpha:1),tile)
            glyph.heightAnchor.constraint(equalToConstant:26).isActive=true
            let caption=label(ui(name),10,.white,tile)
            caption.numberOfLines=1
            caption.adjustsFontSizeToFitWidth=true
            caption.minimumScaleFactor=0.72
            caption.heightAnchor.constraint(equalToConstant:18).isActive=true
            let goal=label(ui(requirement),9,UIColor(red:0.77,green:0.91,blue:1,alpha:1),tile)
            goal.numberOfLines=1;goal.adjustsFontSizeToFitWidth=true
            goal.minimumScaleFactor=0.75
            goal.heightAnchor.constraint(equalToConstant:17).isActive=true
            tile.heightAnchor.constraint(equalToConstant:75).isActive=true
            badgeRow.addArrangedSubview(tile)
        }
        s.addArrangedSubview(badgeRow)
        let allAwards=UIButton(type:.system)
        allAwards.setTitle(ui("★  SVE NAGRADE  ›"),for:.normal)
        allAwards.setTitleColor(.white,for:.normal)
        allAwards.titleLabel?.font=UIFont.systemFont(ofSize:13,weight:.heavy)
        allAwards.backgroundColor=UIColor(red:0.05,green:0.32,blue:0.66,alpha:1)
        allAwards.layer.cornerRadius=16
        allAwards.heightAnchor.constraint(equalToConstant:37).isActive=true
        allAwards.accessibilityLabel=ui("Pogledaj sva postignuća")
        allAwards.addAction(UIAction{_ in self.showAchievements(.profile)},for:.touchUpInside)
        s.addArrangedSubview(allAwards)
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
            tile.accessibilityLabel=ui(BopaviCore.names[world]) + ", " +
                String(progress.worldStars(world)) +
                (progress.languageCode=="hr" ? " od 30 zvjezdica; otvori levele" :
                    " of 30 stars; open levels")
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
            artFrame.heightAnchor.constraint(equalToConstant:59).isActive=true
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
            let title=label(ui(BopaviCore.names[world]).uppercased(),11,.white,content)
            title.numberOfLines=1;title.adjustsFontSizeToFitWidth=true
            title.minimumScaleFactor=0.72
            title.heightAnchor.constraint(equalToConstant:22).isActive=true
            let stars=label("★ "+String(progress.worldStars(world))+"/30",12,
                UIColor(red:1,green:0.77,blue:0.30,alpha:1),content)
            stars.heightAnchor.constraint(equalToConstant:19).isActive=true
            tile.heightAnchor.constraint(equalToConstant:100).isActive=true
            tile.addAction(UIAction{_ in
                self.sound.effect("click");self.showLevels(world,page:1)
            },for:.touchUpInside)
            worldRow.addArrangedSubview(tile)
        }
        s.addArrangedSubview(worldRow)
        let editProfile=BopaviActionButton(primary:true)
        editProfile.setTitle(ui("✎  UREDI PROFIL"),for:.normal)
        editProfile.accessibilityLabel=ui("UREDI PROFIL")
        editProfile.heightAnchor.constraint(equalToConstant:53).isActive=true
        editProfile.addAction(UIAction{_ in self.showSettings()},for:.touchUpInside)
        s.addArrangedSubview(editProfile)
        // Saved playable-pilot identity remains visible and accessible.
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
            ("NavCollection","KOLEKCIJA",{self.showCollection()}),
            ("NavShop","TRGOVINA",{self.showPerks(.profile)})
        ]
        for (index,item) in tabs.enumerated(){
            let tab=UIButton(type:.custom)
            tab.backgroundColor=index==0
                ? UIColor(red:0.60,green:0.36,blue:0.05,alpha:1)
                : UIColor(red:0.04,green:0.31,blue:0.63,alpha:0.8)
            tab.layer.cornerRadius=13
            tab.heightAnchor.constraint(equalToConstant:49).isActive=true
            tab.accessibilityLabel=ui(item.1)+(index==0 ?
                (progress.languageCode=="hr" ? ", odabrano" : ", selected") : "")
            let content=UIStackView()
            content.axis = .vertical;content.translatesAutoresizingMaskIntoConstraints=false
            content.isUserInteractionEnabled=false
            let glyph=UIImageView(image:UIImage(named:item.0))
            glyph.contentMode = .scaleAspectFit
            glyph.heightAnchor.constraint(equalToConstant:25).isActive=true
            content.addArrangedSubview(glyph)
            let name=label(item.1,11,.white,content)
            name.adjustsFontSizeToFitWidth=true;name.minimumScaleFactor=0.72
            name.heightAnchor.constraint(equalToConstant:17).isActive=true
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
        // Top-left native Back control replaces a redundant 58-point footer.
    }

    private func showLeaderboard(){
        showLeaderboardPage(0,fromSettings:currentMenuName=="POSTAVKE")
    }
    private func showLeaderboardPage(_ page:Int,fromSettings:Bool){
        recordsReturnToSettings=fromSettings
        let stack=menu("MOJI REKORDI","Tvojih deset najboljih letova")
        let entries=progress.leaderboard()
        if let best=entries.first {
            let banner=UIStackView()
            banner.axis = .vertical;banner.alignment = .fill;banner.spacing=2
            banner.isLayoutMarginsRelativeArrangement=true
            banner.layoutMargins=UIEdgeInsets(top:9,left:10,bottom:9,right:10)
            banner.backgroundColor=UIColor(red:0.025,green:0.29,blue:0.64,alpha:0.98)
            banner.layer.cornerRadius=21
            banner.layer.borderWidth=2
            banner.layer.borderColor=UIColor(red:1,green:0.78,blue:0.29,alpha:1).cgColor
            banner.isAccessibilityElement=true
            banner.accessibilityLabel="Osobni rekord: \(best.score) bodova"
            label("♛  OSOBNI REKORD",17,
                  UIColor(red:1,green:0.79,blue:0.32,alpha:1),banner)
            let score=label("\(best.score) BODOVA",26,.white,banner)
            score.font=UIFont.monospacedDigitSystemFont(ofSize:26,weight:.heavy)
            stack.addArrangedSubview(banner)
        } else {
            label("Prvi rekord čeka tvoj sljedeći let!",17,.white,stack)
        }
        let perPage=2
        let totalPages=max(1,(entries.count+perPage-1)/perPage)
        let current=max(0,min(page,totalPages-1))
        for (index,item) in entries.enumerated().dropFirst(current*perPage).prefix(perPage) {
            let champion=index==0
            let card=UIStackView()
            card.axis = .horizontal;card.alignment = .center;card.spacing=7
            card.isLayoutMarginsRelativeArrangement=true
            card.layoutMargins=UIEdgeInsets(top:8,left:10,bottom:8,right:10)
            card.backgroundColor=UIColor(red:0.025,green:champion ? 0.29 : 0.16,
                                         blue:champion ? 0.55 : 0.37,alpha:1)
            card.layer.cornerRadius=18
            card.layer.borderWidth=champion ? 2 : 1
            card.layer.borderColor=champion
                ? UIColor(red:1,green:0.78,blue:0.29,alpha:1).cgColor
                : UIColor(red:0.10,green:0.78,blue:1,alpha:1).cgColor
            card.isAccessibilityElement=true
            card.accessibilityLabel="Let \(index+1), \(item.score) bodova, \(BopaviCore.names[item.world]), \(item.gates) prolaza"
            let medal=UILabel()
            medal.text=champion ? "♛" : String(index+1)
            medal.font=UIFont.systemFont(ofSize:champion ? 25:21,weight:.heavy)
            medal.textAlignment = .center
            medal.textColor=champion ?
                UIColor(red:1,green:0.77,blue:0.30,alpha:1) : .white
            medal.widthAnchor.constraint(equalToConstant:40).isActive=true
            card.addArrangedSubview(medal)
            let info=UIStackView()
            info.axis = .vertical;info.alignment = .fill;info.spacing=2
            let score=label("\(item.score) BODOVA",18,.white,info)
            score.font=UIFont.monospacedDigitSystemFont(ofSize:18,weight:.heavy)
            score.adjustsFontSizeToFitWidth=true;score.minimumScaleFactor=0.7
            let location=label("\(BopaviCore.names[item.world]) · \(progress.difficultyNames[item.difficulty])",
                               11,UIColor(red:0.83,green:0.94,blue:1,alpha:1),info)
            location.adjustsFontSizeToFitWidth=true;location.minimumScaleFactor=0.7
            card.addArrangedSubview(info)
            let gates=UILabel()
            gates.text="⚑ \(item.gates)"
            gates.textColor=UIColor(red:1,green:0.77,blue:0.30,alpha:1)
            gates.font=UIFont.monospacedDigitSystemFont(ofSize:12,weight:.heavy)
            gates.setContentCompressionResistancePriority(.required,for:.horizontal)
            card.addArrangedSubview(gates)
            stack.addArrangedSubview(card)
        }
        if totalPages>1 {
            let nav=UIStackView()
            nav.axis = .horizontal;nav.distribution = .fillEqually
            nav.spacing=5
            button("‹ PRETHODNI",in:nav,primary:false){
                if current>0 {self.showLeaderboardPage(current-1,fromSettings:fromSettings)}
            }
            let indicator=label("\(current+1)/\(totalPages)",13,.white,nav)
            indicator.backgroundColor=UIColor(red:0.02,green:0.16,blue:0.35,alpha:1)
            indicator.layer.cornerRadius=16
            indicator.clipsToBounds=true
            button("SLJEDEĆI ›",in:nav,primary:false){
                if current<totalPages-1 {self.showLeaderboardPage(current+1,fromSettings:fromSettings)}
            }
            stack.addArrangedSubview(nav)
        }
        button("★  MOJA POSTIGNUĆA",in:stack,primary:false){self.showAchievements(.records)}
        menuBack(stack){if fromSettings {self.showSettings()} else {self.showHome()}}
    }
    private func showAchievements(){showAchievements(.settings)}
    private func showAchievements(_ origin:MenuOrigin){
        let s=menu("POSTIGNUĆA","Tvoj napredak kroz svih osam svjetova")
        let finished=(0..<8).reduce(0){$0+max(0,progress.frontier($1)-1)}
        let stats=UIStackView()
        stats.axis = .horizontal
        stats.distribution = .fillEqually
        stats.alignment = .center
        stats.backgroundColor=UIColor(red:0.025,green:0.21,blue:0.48,alpha:0.98)
        stats.layer.cornerRadius=18
        stats.layer.borderWidth=2
        stats.layer.borderColor=UIColor(red:1,green:0.79,blue:0.31,alpha:1).cgColor
        stats.isAccessibilityElement=true
        stats.accessibilityLabel="Dovršeni leveli: \(finished), Pobjede: \(progress.wins()), Pokušaji bez pobjede: \(progress.deaths())"
        stats.heightAnchor.constraint(equalToConstant:64).isActive=true
        for (name,value) in [("LEVELI",finished),("POBJEDE",progress.wins()),
                             ("LETOVI",progress.wins()+progress.deaths())] {
            let cell=UILabel()
            cell.text="\(name)\n\(value)"
            cell.numberOfLines=2
            cell.textAlignment = .center
            cell.textColor = .white
            cell.font=UIFont.monospacedDigitSystemFont(ofSize:14,weight:.heavy)
            cell.isAccessibilityElement=false
            stats.addArrangedSubview(cell)
        }
        s.addArrangedSubview(stats)
        let section=label("NAPREDAK PO SVJETOVIMA",14,.white,s)
        section.textAlignment = .left
        section.backgroundColor=UIColor(red:0.025,green:0.16,blue:0.37,alpha:1)
        section.layer.cornerRadius=14
        section.layer.borderWidth=1
        section.layer.borderColor=UIColor(red:0.22,green:0.79,blue:1,alpha:1).cgColor
        section.clipsToBounds=true
        section.heightAnchor.constraint(equalToConstant:34).isActive=true
        for row in 0..<4 {
            let columns=UIStackView()
            columns.axis = .horizontal
            columns.distribution = .fillEqually
            columns.spacing=6
            for col in 0..<2 {
                let w=row*2+col
                let stars=progress.worldStars(w)
                let level=progress.streamFrontier(w)
                let record=progress.best(w)
                let card=UIButton(type:.custom)
                card.backgroundColor=w==progress.chosenWorld()
                    ? UIColor(red:0.05,green:0.28,blue:0.59,alpha:0.98)
                    : UIColor(red:0.025,green:0.15,blue:0.32,alpha:0.98)
                card.layer.cornerRadius=17
                card.layer.borderWidth=2
                card.layer.borderColor=(w==progress.chosenWorld()
                    ? UIColor(red:1,green:0.80,blue:0.32,alpha:1)
                    : worldAccents[w]).cgColor
                card.heightAnchor.constraint(equalToConstant:71).isActive=true
                card.accessibilityLabel="\(BopaviCore.names[w]), zvjezdice \(stars) od 30, level \(level), rekord \(record). Otvori levele"
                let heading=UILabel()
                heading.text=BopaviCore.names[w].uppercased()
                heading.textColor = .white
                heading.font=UIFont.systemFont(ofSize:12,weight:.heavy)
                heading.adjustsFontSizeToFitWidth=true
                heading.minimumScaleFactor=0.76
                let values=UILabel()
                values.text="★ \(stars)/30  ·  ♛ \(record)"
                values.font=UIFont.monospacedDigitSystemFont(ofSize:12,weight:.bold)
                values.textColor=UIColor(red:1,green:0.83,blue:0.39,alpha:1)
                values.adjustsFontSizeToFitWidth=true
                values.minimumScaleFactor=0.8
                let inner=UIStackView(arrangedSubviews:[heading,values])
                inner.axis = .vertical
                inner.spacing=3
                inner.isUserInteractionEnabled=false
                inner.translatesAutoresizingMaskIntoConstraints=false
                card.addSubview(inner)
                NSLayoutConstraint.activate([
                    inner.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:9),
                    inner.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-7),
                    inner.centerYAnchor.constraint(equalTo:card.centerYAnchor)
                ])
                card.addAction(UIAction{[weak self] _ in
                    self?.sound.effect("click")
                    self?.showLevels(w,page:1)
                },for:.touchUpInside)
                columns.addArrangedSubview(card)
            }
            s.addArrangedSubview(columns)
        }
        menuBack(s){
            switch origin {
            case .home: self.showHome()
            case .profile: self.showProfile()
            case .records: self.showLeaderboardPage(0,fromSettings:self.recordsReturnToSettings)
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

    /// Seven concise, safe-area-fitted controls from the BOPAVI reference.
    /// Advanced Settings retains all original preferences and backup actions.
    private func showSettings(){
        clear()
        currentMenuName="POSTAVKE"
        let art=UIImageView(image:UIImage(named:"World0"))
        art.contentMode = .scaleAspectFill
        art.clipsToBounds=true
        art.isAccessibilityElement=false
        art.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(art)
        NSLayoutConstraint.activate([
            art.topAnchor.constraint(equalTo:view.topAnchor),
            art.bottomAnchor.constraint(equalTo:view.bottomAnchor),
            art.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            art.trailingAnchor.constraint(equalTo:view.trailingAnchor)
        ])
        let shade=UIView()
        shade.backgroundColor=UIColor(red:0.015,green:0.14,blue:0.35,alpha:0.20)
        shade.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(shade)
        NSLayoutConstraint.activate([
            shade.topAnchor.constraint(equalTo:view.topAnchor),
            shade.bottomAnchor.constraint(equalTo:view.bottomAnchor),
            shade.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            shade.trailingAnchor.constraint(equalTo:view.trailingAnchor)
        ])
        let stack=UIStackView()
        stack.axis = .vertical
        stack.alignment = .fill
        stack.spacing=5
        stack.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:8),
            stack.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor,constant:-8),
            stack.leadingAnchor.constraint(equalTo:view.safeAreaLayoutGuide.leadingAnchor,constant:16),
            stack.trailingAnchor.constraint(equalTo:view.safeAreaLayoutGuide.trailingAnchor,constant:-16)
        ])
        // Independent layered logo and active-pilot artwork, not a screenshot.
        let brandHero=UIStackView()
        brandHero.axis = .horizontal
        brandHero.alignment = .center
        brandHero.distribution = .fill
        brandHero.spacing=5
        let brandLogo=UIImageView(image:UIImage(named:"Logo"))
        brandLogo.contentMode = .scaleAspectFit
        brandLogo.isAccessibilityElement=false
        brandHero.addArrangedSubview(brandLogo)
        let brandPilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        brandPilot.contentMode = .scaleAspectFit
        brandPilot.isAccessibilityElement=false
        brandPilot.widthAnchor.constraint(equalToConstant:105).isActive=true
        brandHero.addArrangedSubview(brandPilot)
        let heroHeight=min(CGFloat(96),max(CGFloat(65),view.bounds.height*0.11))
        brandHero.heightAnchor.constraint(equalToConstant:heroHeight).isActive=true
        stack.addArrangedSubview(brandHero)
        let header=UIView()
        header.backgroundColor=UIColor(red:0.025,green:0.13,blue:0.34,alpha:0.96)
        header.layer.cornerRadius=22
        header.layer.borderWidth=2
        header.layer.borderColor=UIColor(red:0.26,green:0.86,blue:1,alpha:1).cgColor
        header.heightAnchor.constraint(equalToConstant:60).isActive=true
        stack.addArrangedSubview(header)
        let back=UIButton(type:.system)
        back.setTitle("←",for:.normal)
        back.setTitleColor(.white,for:.normal)
        back.titleLabel?.font=UIFont.systemFont(ofSize:31,weight:.heavy)
        back.backgroundColor=UIColor(red:0.035,green:0.40,blue:0.84,alpha:1)
        back.layer.cornerRadius=25
        back.layer.borderWidth=2
        back.layer.borderColor=UIColor(red:0.75,green:0.97,blue:1,alpha:1).cgColor
        back.accessibilityLabel=progress.languageCode=="hr" ? "Natrag na početnu" : "Back to Home"
        back.translatesAutoresizingMaskIntoConstraints=false
        back.addAction(UIAction{[weak self] _ in self?.showHome()},for:.touchUpInside)
        header.addSubview(back)
        let heading=UILabel()
        heading.text=ui("POSTAVKE")
        let settingsTitleFont=UIFont.systemFont(ofSize:23,weight:.black)
        heading.font=UIFont(descriptor:settingsTitleFont.fontDescriptor.withDesign(.rounded)
            ?? settingsTitleFont.fontDescriptor,size:23)
        heading.textColor = .white
        heading.textAlignment = .center
        heading.translatesAutoresizingMaskIntoConstraints=false
        header.addSubview(heading)
        let chosenPilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        chosenPilot.contentMode = .scaleAspectFit
        chosenPilot.isAccessibilityElement=false
        chosenPilot.translatesAutoresizingMaskIntoConstraints=false
        header.addSubview(chosenPilot)
        NSLayoutConstraint.activate([
            back.leadingAnchor.constraint(equalTo:header.leadingAnchor,constant:8),
            back.centerYAnchor.constraint(equalTo:header.centerYAnchor),
            back.widthAnchor.constraint(equalToConstant:50),
            back.heightAnchor.constraint(equalToConstant:50),
            heading.leadingAnchor.constraint(equalTo:back.trailingAnchor,constant:7),
            heading.trailingAnchor.constraint(equalTo:header.trailingAnchor,constant:-52),
            heading.centerYAnchor.constraint(equalTo:header.centerYAnchor),
            chosenPilot.trailingAnchor.constraint(equalTo:header.trailingAnchor,constant:-8),
            chosenPilot.centerYAnchor.constraint(equalTo:header.centerYAnchor),
            chosenPilot.widthAnchor.constraint(equalToConstant:44),
            chosenPilot.heightAnchor.constraint(equalToConstant:44)
        ])
        // A distinct group stack per section preserves the reference's audio,
        // difficulty, language and additional-options hierarchy.
        var rows=UIStackView()
        func settingsSection(_ title:String){
            let heading=UILabel()
            heading.text=ui(title)
            heading.font=UIFont.systemFont(ofSize:12,weight:.heavy)
            heading.textColor=UIColor(red:1,green:0.81,blue:0.34,alpha:1)
            heading.backgroundColor=UIColor(red:0.025,green:0.17,blue:0.37,alpha:0.95)
            heading.layer.cornerRadius=12
            heading.clipsToBounds=true
            heading.textAlignment = .center
            heading.heightAnchor.constraint(equalToConstant:22).isActive=true
            stack.addArrangedSubview(heading)
            rows=UIStackView()
            rows.axis = .vertical
            rows.alignment = .fill
            rows.distribution = .fillEqually
            rows.spacing=4
            stack.addArrangedSubview(rows)
        }
        settingsSection("IZGLED I ZVUK")
        // Native main-screen sliders: audio adjusts here, no surprise
        // navigation into Advanced Settings or fake percentage buttons.
        func settingsSlider(_ name:String,_ value:Int,onChange:@escaping(Int)->Void) {
            let row=UIStackView()
            row.axis = .horizontal;row.alignment = .center;row.spacing=5
            row.isLayoutMarginsRelativeArrangement=true
            row.layoutMargins=UIEdgeInsets(top:3,left:10,bottom:3,right:9)
            row.backgroundColor=UIColor(red:0.025,green:0.19,blue:0.43,alpha:0.97)
            row.layer.cornerRadius=18
            row.layer.borderWidth=1
            row.layer.borderColor=UIColor(red:0.29,green:0.83,blue:1,alpha:1).cgColor
            let caption=UILabel()
            caption.text=ui(name)
            caption.textColor = .white;caption.font=UIFont.systemFont(ofSize:13,weight:.heavy)
            caption.adjustsFontSizeToFitWidth=true;caption.minimumScaleFactor=0.76
            caption.widthAnchor.constraint(equalToConstant:77).isActive=true
            row.addArrangedSubview(caption)
            let slider=UISlider()
            slider.minimumValue=0;slider.maximumValue=100;slider.value=Float(value)
            slider.minimumTrackTintColor=UIColor(red:0,green:0.84,blue:1,alpha:1)
            slider.maximumTrackTintColor=UIColor(red:0.12,green:0.30,blue:0.57,alpha:1)
            slider.thumbTintColor=UIColor(red:1,green:0.81,blue:0.28,alpha:1)
            slider.accessibilityLabel=ui(name)
            row.addArrangedSubview(slider)
            let percentage=UILabel()
            percentage.text="\(value)%"
            percentage.font=UIFont.monospacedDigitSystemFont(ofSize:12,weight:.heavy)
            percentage.textColor=UIColor(red:1,green:0.82,blue:0.32,alpha:1)
            percentage.textAlignment = .center
            percentage.widthAnchor.constraint(equalToConstant:42).isActive=true
            row.addArrangedSubview(percentage)
            slider.addAction(UIAction{[weak slider,weak percentage] _ in
                guard let slider=slider else{return}
                let updated=max(0,min(100,Int(slider.value.rounded())))
                percentage?.text="\(updated)%"
                onChange(updated)
            },for:.valueChanged)
            rows.addArrangedSubview(row)
        }
        // A real persisted UIKit switch, matching the reference's ON/OFF UI.
        func settingsSwitch(_ name:String,_ enabled:Bool,onChange:@escaping(Bool)->Void) {
            let row=UIStackView()
            row.axis = .horizontal;row.alignment = .center
            row.isLayoutMarginsRelativeArrangement=true
            row.layoutMargins=UIEdgeInsets(top:3,left:12,bottom:3,right:10)
            row.backgroundColor=UIColor(red:0.025,green:0.19,blue:0.43,alpha:0.97)
            row.layer.cornerRadius=18;row.layer.borderWidth=1
            row.layer.borderColor=UIColor(red:0.29,green:0.83,blue:1,alpha:1).cgColor
            let label=UILabel()
            label.text=ui(name);label.textColor = .white
            label.font=UIFont.systemFont(ofSize:14,weight:.heavy)
            row.addArrangedSubview(label)
            let control=UISwitch()
            control.isOn=enabled
            control.onTintColor=UIColor(red:0.05,green:0.70,blue:1,alpha:1)
            control.accessibilityLabel=ui(name)
            control.addAction(UIAction{[weak control] _ in
                if let control=control {onChange(control.isOn)}
            },for:.valueChanged)
            row.addArrangedSubview(control)
            rows.addArrangedSubview(row)
        }
        func settingRow(_ name:String,_ statusText:@escaping()->String,
                        _ press:@escaping()->Void){
            let row=UIButton(type:.custom)
            row.backgroundColor=UIColor(red:0.025,green:0.13,blue:0.31,alpha:0.97)
            row.layer.cornerRadius=18
            row.layer.borderWidth=1
            row.layer.borderColor=UIColor(red:0.26,green:0.83,blue:1,alpha:0.9).cgColor
            row.accessibilityLabel="\(ui(name)), \(ui(statusText()))"
            let label=UILabel()
            label.text=ui(name)
            label.font=UIFont.systemFont(ofSize:14,weight:.heavy)
            label.textColor = .white
            label.adjustsFontSizeToFitWidth=true
            label.minimumScaleFactor=0.75
            label.translatesAutoresizingMaskIntoConstraints=false
            label.isUserInteractionEnabled=false
            row.addSubview(label)
            let status=UILabel()
            status.text=ui(statusText())
            status.font=UIFont.monospacedDigitSystemFont(ofSize:12,weight:.bold)
            status.textColor = .white
            status.backgroundColor=UIColor(red:0.04,green:0.43,blue:0.80,alpha:1)
            status.layer.cornerRadius=12
            status.clipsToBounds=true
            status.textAlignment = .center
            status.translatesAutoresizingMaskIntoConstraints=false
            status.isUserInteractionEnabled=false
            row.addSubview(status)
            NSLayoutConstraint.activate([
                label.leadingAnchor.constraint(equalTo:row.leadingAnchor,constant:12),
                label.centerYAnchor.constraint(equalTo:row.centerYAnchor),
                label.trailingAnchor.constraint(lessThanOrEqualTo:status.leadingAnchor,constant:-6),
                status.trailingAnchor.constraint(equalTo:row.trailingAnchor,constant:-8),
                status.centerYAnchor.constraint(equalTo:row.centerYAnchor),
                status.widthAnchor.constraint(equalToConstant:99),
                status.heightAnchor.constraint(equalToConstant:31)
            ])
            row.addAction(UIAction{[weak self, weak row, weak status] _ in
                guard let self=self else{return}
                press()
                status?.text=self.ui(statusText())
                row?.accessibilityLabel="\(self.ui(name)), \(self.ui(statusText()))"
            },for:.touchUpInside)
            rows.addArrangedSubview(row)
        }
        settingsSlider("GLAZBA",progress.musicVolume){[weak self] value in
            self?.progress.musicVolume=value
            self?.sound.musicVolume=Float(value)/100
        }
        settingsSwitch("VIBRACIJA",progress.hapticEnabled){[weak self] value in
            self?.progress.hapticEnabled=value
        }
        settingsSlider("EFEKTI",progress.effectsVolume){[weak self] value in
            self?.progress.effectsVolume=value
            self?.sound.effectsVolume=Float(value)/100
        }
        // All three real difficulty modes are accessible directly in Settings,
        // in the same row previously used by the redundant Privacy shortcut.
        // Legal/privacy actions remain permanently available in the footer.
        settingsSection("TEŽINA IGRE")
        let difficultyRow=UIStackView()
        difficultyRow.axis = .horizontal
        difficultyRow.distribution = .fillEqually
        difficultyRow.spacing=7
        let modeTitles=["LAGANO","NORMALNO","ZAHTJEVNO"]
        for mode in 0..<3 {
            let card=UIButton(type:.system)
            card.tag=mode
            card.setTitle((mode==1 ? "♛  " : "✦  ")+ui(modeTitles[mode]),for:.normal)
            card.titleLabel?.font=UIFont.systemFont(ofSize:12,weight:.heavy)
            card.titleLabel?.adjustsFontSizeToFitWidth=true
            card.titleLabel?.minimumScaleFactor=0.72
            card.setTitleColor(.white,for:.normal)
            card.layer.cornerRadius=17
            card.layer.borderWidth=2
            card.isAccessibilityElement=true
            card.accessibilityLabel=(progress.languageCode=="hr" ? "Težina igre" : "Game difficulty") + ", " + ui(modeTitles[mode])
            let chosen=progress.difficulty==mode
            card.accessibilityTraits=chosen ? [.button,.selected] : .button
            card.backgroundColor=chosen ?
                UIColor(red:0.60,green:0.33,blue:0.035,alpha:1) :
                UIColor(red:0.025,green:0.25,blue:0.55,alpha:1)
            card.layer.borderColor=(chosen ?
                UIColor(red:1,green:0.81,blue:0.28,alpha:1) :
                UIColor(red:0.24,green:0.81,blue:1,alpha:1)).cgColor
            card.addAction(UIAction{[weak self,weak difficultyRow] _ in
                guard let self=self else{return}
                self.progress.difficulty=mode
                self.sound.effect("click")
                for case let option as UIButton in difficultyRow?.arrangedSubviews ?? [] {
                    let active=option.tag==mode
                    option.accessibilityTraits=active ? [.button,.selected] : .button
                    option.backgroundColor=active ?
                        UIColor(red:0.60,green:0.33,blue:0.035,alpha:1) :
                        UIColor(red:0.025,green:0.25,blue:0.55,alpha:1)
                    option.layer.borderColor=(active ?
                        UIColor(red:1,green:0.81,blue:0.28,alpha:1) :
                        UIColor(red:0.24,green:0.81,blue:1,alpha:1)).cgColor
                }
            },for:.touchUpInside)
            difficultyRow.addArrangedSubview(card)
        }
        rows.addArrangedSubview(difficultyRow)
        settingsSection("JEZIK")
        settingRow("JEZIK",{[weak self] in GameLanguages.name(self?.progress.languageCode ?? "en")}){
            [weak self] in self?.showLanguagePicker()
        }
        settingsSection("DODATNE OPCIJE")
        settingRow("ZVUK",{[weak self] in self?.progress.soundEnabled == true ? "UKLJ." : "ISKL."}){
            [weak self] in
            guard let self=self else{return}
            let value = !self.progress.soundEnabled
            self.progress.soundEnabled=value
            self.sound.enabled=value
        }
        settingsSwitch("MANJE ANIMACIJA",progress.lessMotion){[weak self] value in
            guard let self=self else{return}
            if value != self.progress.lessMotion {
                self.progress.lessMotion = !self.progress.lessMotion
                self.showSettings()
            }
        }
        let advanced=UIButton(type:.system)
        advanced.setTitle(ui("⚙  NAPREDNE POSTAVKE"),for:.normal)
        advanced.setTitleColor(.white,for:.normal)
        advanced.titleLabel?.font=UIFont.systemFont(ofSize:15,weight:.heavy)
        advanced.backgroundColor=UIColor(red:0.025,green:0.38,blue:0.78,alpha:1)
        advanced.layer.cornerRadius=20
        advanced.layer.borderWidth=2
        advanced.layer.borderColor=UIColor(red:0.23,green:0.85,blue:1,alpha:1).cgColor
        advanced.heightAnchor.constraint(equalToConstant:49).isActive=true
        advanced.accessibilityLabel=ui("NAPREDNE POSTAVKE")
        advanced.addAction(UIAction{[weak self] _ in self?.showSettingsDetails()},for:.touchUpInside)
        stack.addArrangedSubview(advanced)

        // Real on-device legal controls, independent of network and web views.
        let legal=UIStackView()
        legal.axis = .horizontal
        legal.distribution = .fillEqually
        legal.spacing=7
        legal.heightAnchor.constraint(equalToConstant:35).isActive=true
        for (title,privacy) in [("POLITIKA PRIVATNOSTI",true),
                                ("UVJETI KORIŠTENJA",false)] {
            let link=UIButton(type:.system)
            link.setTitle(ui(title),for:.normal)
            link.titleLabel?.font=UIFont.systemFont(ofSize:10,weight:.bold)
            link.titleLabel?.adjustsFontSizeToFitWidth=true
            link.titleLabel?.minimumScaleFactor=0.78
            link.setTitleColor(.white,for:.normal)
            link.backgroundColor=UIColor(red:0.025,green:0.16,blue:0.37,alpha:0.97)
            link.layer.cornerRadius=12
            link.layer.borderWidth=1
            link.layer.borderColor=UIColor(red:0.27,green:0.80,blue:1,alpha:1).cgColor
            link.accessibilityLabel=ui(title)
            link.addAction(UIAction{[weak self] _ in
                self?.showLegalNotice(privacy:privacy)
            },for:.touchUpInside)
            legal.addArrangedSubview(link)
        }
        stack.addArrangedSubview(legal)
        let publisher=UILabel()
        publisher.text="© BRENDIGO  ·  BOPAVI"
        publisher.textColor=UIColor(red:0.87,green:0.96,blue:1,alpha:1)
        publisher.font=UIFont.systemFont(ofSize:10,weight:.bold)
        publisher.textAlignment = .center
        publisher.heightAnchor.constraint(equalToConstant:19).isActive=true
        stack.addArrangedSubview(publisher)
    }

    private func showLegalNotice(privacy:Bool){
        let title=privacy ? "POLITIKA PRIVATNOSTI" : "UVJETI KORIŠTENJA"
        let details=progress.languageCode=="hr" ? (privacy
            ? "BOPAVI izdaje Brendigo. Igra nema račune, oglase, analitiku ili kupnje stvarnim novcem. Nadimak, rezultati, virtualne kovanice, napredak i postavke ostaju lokalno na uređaju. Dobrovoljni JSON izvoz koristi odabranu lokaciju, uključujući vanjske pružatelje. Podaci se ne šalju poslužiteljima izdavača. Izbriši podatke aplikacije ili je deinstaliraj za lokalno brisanje; izvezene kopije brišu se zasebno. Kontakt: github.com/bren-wp/BOPAVI/issues."
            : "BOPAVI je igra izdavača Brendigo. Igraj odgovorno. Kovanice, zvjezdice i nagrade imaju vrijednost samo unutar igre; nema plaćanja stvarnim novcem. Napredak ostaje na uređaju uz dobrovoljni izvoz i uvoz. Mlađim igračima preporučuje se roditeljsko vodstvo. Podrška: github.com/bren-wp/BOPAVI/issues. Primjenjuju se obvezna potrošačka prava."
) : (privacy ? "BOPAVI is published by Brendigo. The game uses no accounts, ads, analytics or real-money purchases. Nickname, scores, virtual coins, progress and settings remain on this device and are not sent to the publisher. Exporting a JSON backup is optional and uses the location you select, which may include another provider. Clear the app data or uninstall to erase local data; delete exported backups separately. Support: github.com/bren-wp/BOPAVI/issues." : "BOPAVI is a game published by Brendigo. Play responsibly. Coins, stars and rewards have no cash value; there are no real-money purchases. Progress stays on your device with optional backup export and import. Parental guidance is recommended for younger players. Mandatory consumer rights remain applicable. Support: github.com/bren-wp/BOPAVI/issues.")
        let notice=UIAlertController(title:ui(title),message:details,preferredStyle:.alert)
        notice.addAction(UIAlertAction(title:progress.languageCode=="hr" ? "ZATVORI" : "CLOSE",style:.default))
        present(notice,animated:true)
    }

    private func showSettingsDetails(){
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
        toggle.addAction(UIAction{_ in
            self.progress.lessMotion=toggle.isOn
            self.showSettingsDetails()
        },for:.valueChanged)
        let audio=UISwitch();audio.isOn=progress.soundEnabled
        audio.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        let audioRow=UIStackView();audioRow.axis = .horizontal;audioRow.spacing=12
        let audioLabel=UILabel();audioLabel.text="Glazba i zvučni efekti";audioLabel.font=UIFont.systemFont(ofSize:16,weight:.medium);audioLabel.textColor = .white
        audioLabel.numberOfLines=0;audioLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        audioRow.addArrangedSubview(audioLabel);audioRow.addArrangedSubview(audio);settingsPanel.addArrangedSubview(audioRow)
        audio.addAction(UIAction{_ in self.progress.soundEnabled=audio.isOn;self.sound.enabled=audio.isOn},for:.valueChanged)

        label("Odabrani pilot: \(progress.skinNames[progress.skinIndex()]) · Oprema vrijedi za sve pilote.",14,.white,settingsPanel)
        button("🛍  TRGOVINA KOVANICAMA",in:settingsPanel,primary:false){self.showPerks()}
        button("🏆  MOJI REKORDI",in:settingsPanel,primary:false){self.showLeaderboard()}
        sectionHeading("PODACI I PRIVATNOST",in:settingsPanel)
        label("BOPAVI — Mali let, velika avantura. Stvorio Brendigo.",14,.white,settingsPanel)
        label("Bez oglasa, praćenja i kupnje stvarnim novcem.",14,.white,settingsPanel)
        button("SPREMI KOPIJU NAPRETKA",in:settingsPanel,primary:false){
            do {
                // Each voluntary export is unique, protected, and deleted once shared.
                let url=FileManager.default.temporaryDirectory
                    .appendingPathComponent("bopavi-save-\(UUID().uuidString).json")
                try self.progress.exportData().write(to:url,options:[.atomic,.completeFileProtectionUnlessOpen])
                let activity=UIActivityViewController(activityItems:[url],applicationActivities:nil)
                activity.completionWithItemsHandler={_,_,_,_ in
                    try? FileManager.default.removeItem(at:url)
                }
                activity.popoverPresentationController?.sourceView=self.view
                self.present(activity,animated:true)
            } catch {self.alert("Izvoz nije uspio",error.localizedDescription)}
        }
        button("VRATI NAPREDAK IZ KOPIJE",in:settingsPanel,primary:false){
            let picker=UIDocumentPickerViewController(forOpeningContentTypes:[.json],asCopy:true)
            picker.delegate=self;self.present(picker,animated:true)
        }
        menuBack(s){self.showSettings()}
    }

    /// Saved progress only: eight discoverable worlds, collected tokens and local pilots.
    /// Illustrated cards contain real UIKit actions, never screenshot hotspots.
    private func showCollection(){
        let croatian=progress.languageCode=="hr"
        let s=menu("KOLEKCIJA",croatian ? "Tvoja otkrića u osam svjetova" :
            "Your discoveries across eight worlds")
        menuBack(s){self.showHome()}
        let owned=(0...8).filter{progress.owned($0)}.count
        let stars=(0..<8).reduce(0){$0+progress.worldStars($1)}
        let collectionTotals="♛  \(progress.coins()) " + (croatian ? "kovanica" : "coins") + "   ·   ★ \(stars) " + (croatian ? "zvjezdica" : "stars")
        label(collectionTotals,14,.white,s)
        sectionHeading(croatian ? "MOJA OTKRIĆA" : "MY DISCOVERIES",in:s)
        for line in 0..<4 {
            let row=UIStackView()
            row.axis = .horizontal;row.alignment = .fill
            row.distribution = .fillEqually;row.spacing=6
            for col in 0..<2 {
                let world=line*2+col
                let card=UIButton(type:.custom)
                card.layer.cornerRadius=17;card.layer.borderWidth=2
                card.layer.borderColor=(world==progress.chosenWorld()
                    ? UIColor(red:1,green:0.82,blue:0.30,alpha:1)
                    : UIColor(red:0.15,green:0.82,blue:1,alpha:1)).cgColor
                card.clipsToBounds=true
                card.heightAnchor.constraint(equalToConstant:84).isActive=true
                card.accessibilityLabel="\(ui(BopaviCore.names[world])), \(progress.worldStars(world)) stars, \(progress.collectibles(world)) collected"
                card.accessibilityHint=ui("Open world levels")
                let scene=UIImageView(image:UIImage(named:"World\(world)"))
                scene.translatesAutoresizingMaskIntoConstraints=false
                scene.contentMode = .scaleAspectFill;scene.clipsToBounds=true
                scene.isUserInteractionEnabled=false;scene.accessibilityElementsHidden=true
                card.addSubview(scene)
                let shade=UIView()
                shade.translatesAutoresizingMaskIntoConstraints=false
                shade.backgroundColor=UIColor(red:0.01,green:0.07,blue:0.19,alpha:0.50)
                shade.isUserInteractionEnabled=false
                card.addSubview(shade)
                let heading=UILabel()
                heading.translatesAutoresizingMaskIntoConstraints=false
                heading.text=ui(BopaviCore.names[world]).uppercased()
                heading.font=UIFont.systemFont(ofSize:13,weight:.black)
                heading.textColor = .white;heading.adjustsFontSizeToFitWidth=true
                heading.minimumScaleFactor=0.70
                heading.isUserInteractionEnabled=false
                card.addSubview(heading)
                let progressLabel=UILabel()
                progressLabel.translatesAutoresizingMaskIntoConstraints=false
                progressLabel.text="★ \(progress.worldStars(world))/30   ·   ✦ \(progress.collectibles(world))"
                progressLabel.textColor=UIColor(red:1,green:0.83,blue:0.33,alpha:1)
                progressLabel.font=UIFont.monospacedDigitSystemFont(ofSize:12,weight:.bold)
                progressLabel.adjustsFontSizeToFitWidth=true
                progressLabel.minimumScaleFactor=0.7
                progressLabel.isUserInteractionEnabled=false
                card.addSubview(progressLabel)
                NSLayoutConstraint.activate([
                    scene.leadingAnchor.constraint(equalTo:card.leadingAnchor),
                    scene.trailingAnchor.constraint(equalTo:card.trailingAnchor),
                    scene.topAnchor.constraint(equalTo:card.topAnchor),
                    scene.bottomAnchor.constraint(equalTo:card.bottomAnchor),
                    shade.leadingAnchor.constraint(equalTo:card.leadingAnchor),
                    shade.trailingAnchor.constraint(equalTo:card.trailingAnchor),
                    shade.topAnchor.constraint(equalTo:card.topAnchor),
                    shade.bottomAnchor.constraint(equalTo:card.bottomAnchor),
                    heading.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:9),
                    heading.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-4),
                    heading.bottomAnchor.constraint(equalTo:progressLabel.topAnchor,constant:-3),
                    progressLabel.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:9),
                    progressLabel.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-4),
                    progressLabel.bottomAnchor.constraint(equalTo:card.bottomAnchor,constant:-8)
                ])
                card.addAction(UIAction{[weak self] _ in
                    self?.sound.effect("click")
                    self?.showLevels(world,page:1)
                },for:.touchUpInside)
                row.addArrangedSubview(card)
            }
            s.addArrangedSubview(row)
        }
        sectionHeading(croatian ? "MOJI PILOTI" : "MY PILOTS",in:s)
        let selected=progress.skinIndex()
        let pilotRow=UIStackView()
        pilotRow.axis = .horizontal;pilotRow.alignment = .center;pilotRow.spacing=9
        pilotRow.layoutMargins=UIEdgeInsets(top:3,left:9,bottom:3,right:9)
        pilotRow.isLayoutMarginsRelativeArrangement=true
        pilotRow.backgroundColor=UIColor(red:0.02,green:0.18,blue:0.41,alpha:0.98)
        pilotRow.layer.cornerRadius=18;pilotRow.layer.borderWidth=2
        pilotRow.layer.borderColor=UIColor(red:1,green:0.78,blue:0.25,alpha:1).cgColor
        let avatar=UIImageView(image:UIImage(named:"BopiPortrait\(selected)"))
        avatar.contentMode = .scaleAspectFit;avatar.isAccessibilityElement=false
        avatar.widthAnchor.constraint(equalToConstant:54).isActive=true
        avatar.heightAnchor.constraint(equalToConstant:54).isActive=true
        pilotRow.addArrangedSubview(avatar)
        let pilotCaption=UILabel()
        pilotCaption.text="\(progress.skinNames[selected]) · \(owned)/9 " + (croatian ? "otključanih pilota" : "pilots unlocked")
        pilotCaption.textColor = .white
        pilotCaption.font=UIFont.systemFont(ofSize:13,weight:.heavy)
        pilotCaption.adjustsFontSizeToFitWidth=true;pilotCaption.minimumScaleFactor=0.70
        pilotRow.addArrangedSubview(pilotCaption)
        s.addArrangedSubview(pilotRow)
        button(croatian ? "ODABERI LIKA" : "CHOOSE PILOT",in:s,primary:false){
            let world=self.progress.chosenWorld()
            self.showPilotPicker(world,self.progress.streamFrontier(world))
        }
        button(croatian ? "MOJI REKORDI" : "MY RECORDS",in:s,primary:false){
            self.showLeaderboard()
        }
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
        catch {alert("Uvoz nije uspio","Provjeri da je odabrana valjana BOPAVI kopija napretka.")}
    }
    private func alert(_ title:String,_ message:String){let a=UIAlertController(title:title,message:message,preferredStyle:.alert);a.addAction(UIAlertAction(title:"U redu",style:.default));present(a,animated:true)}
}
