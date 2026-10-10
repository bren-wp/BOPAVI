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

/** Native UIKit menus and Core Graphics gameplay, no WKWebView. */
final class GameController: UIViewController, UIDocumentPickerDelegate {
    private let progress=ProgressStore()
    private let sound=Soundscape()
    private let collectHaptic=UISelectionFeedbackGenerator()
    private let shieldHaptic=UIImpactFeedbackGenerator(style:.medium)
    private var canvas:GameCanvas?
    private var gameWorld=0
    private var gameNumber=1
    private var hud:UILabel?
    private var backgroundGradient:CAGradientLayer?
    private let worldAccents:[UIColor] = [
        UIColor(red:0.34,green:0.94,blue:0.68,alpha:1), UIColor(red:1,green:0.75,blue:0.43,alpha:1),
        UIColor(red:0.65,green:0.92,blue:1,alpha:1), UIColor(red:1,green:0.51,blue:0.36,alpha:1),
        UIColor(red:1,green:0.87,blue:0.57,alpha:1), UIColor(red:0.72,green:0.63,blue:1,alpha:1),
        UIColor(red:0.38,green:0.94,blue:0.87,alpha:1), UIColor(red:0.76,green:0.70,blue:1,alpha:1)
    ]
    override var preferredStatusBarStyle:UIStatusBarStyle {.lightContent}
    override var prefersStatusBarHidden:Bool { canvas != nil }
    override var prefersHomeIndicatorAutoHidden:Bool { canvas != nil }
    override func viewDidLoad(){
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self,selector:#selector(pauseForInterruption(_:)),
            name:UIApplication.willResignActiveNotification,object:nil)
        NotificationCenter.default.addObserver(self,selector:#selector(resumeIdlePreview(_:)),
            name:UIApplication.didBecomeActiveNotification,object:nil)
        sound.enabled=progress.soundEnabled
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
        canvas?.stop();canvas=nil;hud=nil;sound.stop()
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
            stack.addArrangedSubview(logo)
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
        // Artwork and labels occupy separate zones, matching Android's world cards.
        // All eight worlds remain immediately playable; the highlight indicates
        // only the user's currently selected world, never a paid/locked world.
        let selected=progress.chosenWorld()==world
        let tile=BopaviActionButton(primary:false)
        tile.setTitle("",for:.normal)
        tile.layer.borderWidth=selected ? 3 : 1
        tile.layer.borderColor=worldAccents[world].withAlphaComponent(selected ? 1 : 0.65).cgColor
        tile.accessibilityLabel="\(BopaviCore.names[world]), otključano\(selected ? ", odabrano" : "")"
        tile.heightAnchor.constraint(equalToConstant:214).isActive=true
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
                preview.heightAnchor.constraint(equalToConstant:137)
            ])
        }
        let headline=UILabel()
        headline.translatesAutoresizingMaskIntoConstraints=false
        headline.text="\(selected ? "✓ " : "")\(BopaviCore.collectibleIcons[world])  \(BopaviCore.names[world])"
        headline.textColor=worldAccents[world]
        headline.font=UIFont.systemFont(ofSize:14,weight:.heavy)
        headline.adjustsFontSizeToFitWidth=true
        headline.minimumScaleFactor=0.72
        headline.textAlignment = .center
        headline.isUserInteractionEnabled=false
        headline.accessibilityElementsHidden=true
        tile.addSubview(headline)
        let detail=UILabel()
        detail.translatesAutoresizingMaskIntoConstraints=false
        detail.text="Level \(progress.streamFrontier(world)) · \(BopaviCore.collectibles[world])"
        detail.textColor=UIColor(red:0.81,green:0.91,blue:0.98,alpha:1)
        detail.font=UIFont.systemFont(ofSize:12,weight:.medium)
        detail.adjustsFontSizeToFitWidth=true
        detail.minimumScaleFactor=0.7
        detail.textAlignment = .center
        detail.isUserInteractionEnabled=false
        detail.accessibilityElementsHidden=true
        tile.addSubview(detail)
        NSLayoutConstraint.activate([
            headline.topAnchor.constraint(equalTo:tile.topAnchor,constant:150),
            headline.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:6),
            headline.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-6),
            headline.heightAnchor.constraint(equalToConstant:23),
            detail.topAnchor.constraint(equalTo:headline.bottomAnchor,constant:3),
            detail.leadingAnchor.constraint(equalTo:tile.leadingAnchor,constant:6),
            detail.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-6),
            detail.heightAnchor.constraint(equalToConstant:20)
        ])
        tile.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        stack.addArrangedSubview(tile)
    }
    private func showHome(){
        let stack=menu("BOPAVI","")
        view.backgroundColor=UIColor(red:0.04,green:0.45,blue:0.79,alpha:1)
        if let artwork=UIImage(named:"LaunchArt") {
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
        let walletRow=UIStackView()
        walletRow.axis = .horizontal;walletRow.alignment = .fill
        walletRow.distribution = .fillEqually;walletRow.spacing=10
        for item in ["🏆  \(progress.bestPoints())","●  \(progress.coins())"] {
            let chip=UILabel()
            chip.text=item
            chip.textColor = .white
            chip.font=UIFont.systemFont(ofSize:16,weight:.heavy)
            chip.textAlignment = .center
            chip.adjustsFontSizeToFitWidth=true
            chip.minimumScaleFactor=0.75
            chip.backgroundColor=UIColor(red:0.03,green:0.14,blue:0.35,alpha:0.93)
            chip.layer.cornerRadius=21;chip.clipsToBounds=true
            chip.layer.borderWidth=1
            chip.layer.borderColor=UIColor(red:0.40,green:0.84,blue:1,alpha:0.9).cgColor
            chip.heightAnchor.constraint(equalToConstant:44).isActive=true
            walletRow.addArrangedSubview(chip)
        }
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
            ("♙","PROFIL",{self.showSettings()}),
            ("★","ZADACI",{self.showAchievements()}),
            ("✦","KOLEKCIJA",{self.showSkins()}),
            ("▣","TRGOVINA",{self.showPerks()})
        ]
        for (icon,title,handler) in tabs {
            let tab=UIButton(type:.system)
            tab.setTitle("",for:.normal)
            tab.accessibilityLabel=title
            tab.backgroundColor=UIColor(red:0.04,green:0.31,blue:0.63,alpha:0.70)
            tab.layer.cornerRadius=13
            tab.heightAnchor.constraint(equalToConstant:62).isActive=true
            let glyph=UILabel()
            glyph.text=icon;glyph.textAlignment = .center
            glyph.textColor = title=="TRGOVINA"
                ? UIColor(red:1,green:0.78,blue:0.25,alpha:1) : .white
            glyph.font=UIFont.systemFont(ofSize:25,weight:.semibold)
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
        let s=menu(BopaviCore.names[world],"Odaberi otključani level")
        let maxNumber=min(BopaviCore.levelsPerWorld,progress.frontier(world))
        label("Otključano do levela \(maxNumber)",18,.white,s)
        button("▶  NASTAVI LET",in:s){self.showPilotPicker(world,self.progress.streamFrontier(world))}
        let field=UITextField()
        field.keyboardType = .numberPad;field.text=String(page);field.placeholder="Broj levela"
        field.textAlignment = .center;field.textColor = .white;field.backgroundColor = UIColor(red:0.10,green:0.24,blue:0.45,alpha:1)
        field.layer.cornerRadius=12;field.heightAnchor.constraint(equalToConstant:52).isActive=true
        s.addArrangedSubview(field)
        button("▶  POKRENI ODABRANI LEVEL",in:s){
            field.resignFirstResponder()
            guard let n=Int(field.text ?? ""), (1...maxNumber).contains(n) else {self.alert("Nedostupan level","Odaberi otključan level iz raspona 1–\(maxNumber).");return}
            self.showPilotPicker(world,n)
        }
        let start=((max(1,min(page,BopaviCore.levelsPerWorld))-1)/20)*20+1
        let zone=BopaviCore.create(world,start).zone
        label("ZONA \(zone) · LEVELI \(start)–\(min(start+19,BopaviCore.levelsPerWorld))",16,.white,s)
        label("● NORMALNI · ⚡ IZAZOVNI · ✦ BONUS · ♛ ELITNI",13,.white,s)
        for rowNumber in 0..<5 {
            let row=UIStackView();row.axis = .horizontal;row.spacing=6
            row.distribution = .fillEqually
            for column in 0..<4 {
                let n=start+rowNumber*4+column
                if n>BopaviCore.levelsPerWorld {break}
                let unlocked=n<=maxNumber
                // Derive the badge from the actual existing procedural level.
                let kind=BopaviCore.create(world,n).type
                let cell=BopaviActionButton(primary:false)
                cell.setTitle("\(unlocked ? LevelKind.icon(kind) : "🔒")\n\(n)",for:.normal)
                cell.alpha=unlocked ? 1 : 0.60
                cell.titleLabel?.numberOfLines=2
                cell.titleLabel?.textAlignment = .center
                cell.titleLabel?.font=UIFont.monospacedDigitSystemFont(ofSize:14,weight:.bold)
                let accent:UIColor
                switch kind {
                case 1: accent=UIColor(red:1,green:0.67,blue:0.33,alpha:1)
                case 2: accent=UIColor(red:1,green:0.87,blue:0.34,alpha:1)
                case 3: accent=UIColor(red:0.81,green:0.63,blue:1,alpha:1)
                default: accent=UIColor(red:0.55,green:0.84,blue:1,alpha:1)
                }
                cell.layer.borderColor=(unlocked ? accent : UIColor.white.withAlphaComponent(0.18)).cgColor
                cell.layer.borderWidth=unlocked && n==maxNumber ? 3 : 1
                let status = !unlocked ? "zaključan" : (n<maxNumber ? "dovršen" : "otključan")
                cell.accessibilityLabel="Level \(n), \(LevelKind.name(kind)), \(status)"
                cell.accessibilityHint=unlocked ? "Pokreni level" : "Prvo dovrši prethodni level"
                cell.heightAnchor.constraint(equalToConstant:63).isActive=true
                cell.addAction(UIAction{_ in
                    self.sound.effect("click")
                    if unlocked {self.showPilotPicker(world,n)}
                    else {self.alert("Zaključano","Prvo dovrši prethodni level.")}
                },for:.touchUpInside)
                row.addArrangedSubview(cell)
            }
            if !row.arrangedSubviews.isEmpty {s.addArrangedSubview(row)}
        }
        if start>1 {button("← Prethodnih 20",in:s,primary:false){self.showLevels(world,page:start-20)}}
        if start+20<=maxNumber {button("Sljedećih 20 →",in:s,primary:false){self.showLevels(world,page:start+20)}}
        button("‹  Svjetovi",in:s,primary:false){self.showWorlds()}
    }
    /// Shared image gallery: every player sees the actual unlocked/locked cast.
    /// A new virtual-coin purchase always requires a second explicit approval.
    private func characterGallery(in stack:UIStackView,refresh:@escaping()->Void) {
        for start in stride(from:0,to:progress.skinNames.count,by:3) {
            let row=UIStackView()
            row.axis = .horizontal;row.alignment = .fill
            row.distribution = .fillEqually;row.spacing=5
            stack.addArrangedSubview(row)
            for i in start..<min(start+3,progress.skinNames.count) {
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
                card.heightAnchor.constraint(equalToConstant:141).isActive=true
                let portrait=UIImageView(image:UIImage(named:"Bopi\(i)"))
                portrait.translatesAutoresizingMaskIntoConstraints=false
                portrait.contentMode = .scaleAspectFit
                portrait.isUserInteractionEnabled=false
                portrait.accessibilityElementsHidden=true
                card.addSubview(portrait)
                let name=UILabel()
                name.translatesAutoresizingMaskIntoConstraints=false
                name.text=progress.skinNames[i]
                name.font=UIFont.systemFont(ofSize:13,weight:.heavy)
                name.textColor = .white;name.textAlignment = .center
                name.adjustsFontSizeToFitWidth=true;name.minimumScaleFactor=0.7
                name.accessibilityElementsHidden=true
                card.addSubview(name)
                let price=UILabel()
                price.translatesAutoresizingMaskIntoConstraints=false
                price.text=status;price.font=UIFont.systemFont(ofSize:10,weight:.bold)
                price.textColor=selected
                    ? UIColor(red:1,green:0.86,blue:0.40,alpha:1)
                    : UIColor(red:0.82,green:0.93,blue:1,alpha:1)
                price.textAlignment = .center
                price.adjustsFontSizeToFitWidth=true;price.minimumScaleFactor=0.72
                price.accessibilityElementsHidden=true
                card.addSubview(price)
                NSLayoutConstraint.activate([
                    portrait.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:8),
                    portrait.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-8),
                    portrait.topAnchor.constraint(equalTo:card.topAnchor,constant:3),
                    portrait.heightAnchor.constraint(equalToConstant:84),
                    name.topAnchor.constraint(equalTo:portrait.bottomAnchor,constant:1),
                    name.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:4),
                    name.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-4),
                    name.heightAnchor.constraint(equalToConstant:20),
                    price.topAnchor.constraint(equalTo:name.bottomAnchor,constant:1),
                    price.leadingAnchor.constraint(equalTo:card.leadingAnchor,constant:4),
                    price.trailingAnchor.constraint(equalTo:card.trailingAnchor,constant:-4),
                    price.heightAnchor.constraint(equalToConstant:22)
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
        let preview=UIImageView(image:UIImage(named:"Bopi\(selected)"))
        preview.translatesAutoresizingMaskIntoConstraints=false
        preview.contentMode = .scaleAspectFit
        preview.backgroundColor=UIColor(red:0.10,green:0.32,blue:0.61,alpha:0.88)
        preview.layer.cornerRadius=21
        preview.clipsToBounds=true
        preview.heightAnchor.constraint(equalToConstant:168).isActive=true
        preview.isAccessibilityElement=true
        preview.accessibilityLabel="Pregled lika \(progress.skinNames[selected])"
        s.addArrangedSubview(preview)
        label(progress.skinNames[selected],26,UIColor(red:1,green:0.86,blue:0.49,alpha:1),s)
        switch selected {
        case 6: label("Portantin: krilati čovječuljak sa zaštitnom opremom i suputnikom.",15,.white,s)
        case 7: label("Noa: premium nebeski istraživač s električno plavim krilima, vizir-naočalama i zvjezdanim oklopom.",15,.white,s)
        case 8: label("Any: premium čarobnica s ružičastim krilima, zvjezdanom tijarom i ljubičastom haljinom.",15,.white,s)
        default: label("Izaberi svog letača. Odabir se čuva na uređaju.",15,.white,s)
        }
        label("\(BopaviCore.names[world]) · Level \(number) · \(progress.coins()) kovanica",15,.white,s)
        button("▶  POLETI S \(progress.skinNames[selected].uppercased())",in:s) {
            self.startGame(world,number)
        }
        sectionHeading("ODABERI SVOG LETAČA",in:s)
        characterGallery(in:s){self.showPilotPicker(world,number)}
        button("‹  LEVELI",in:s,primary:false){self.showLevels(world,page:min(number,BopaviCore.levelsPerWorld))}
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
            let reward=self.progress.completeLevel(world:world,number:level,score:game.score())
            self.sound.effect("level")
            if reward>0 {self.showToast("Level \(level): +\(reward) kovanica")}
        }
        let counter=UILabel();counter.text=" Level \(number) · \(BopaviCore.collectibleIcons[world]) 0 "
        counter.backgroundColor=UIColor(red:0.05,green:0.15,blue:0.38,alpha:0.85)
        counter.font=UIFont.monospacedDigitSystemFont(ofSize:16,weight:.heavy)
        counter.textColor = .white;counter.layer.cornerRadius=15;counter.clipsToBounds=true
        counter.adjustsFontSizeToFitWidth=true;counter.minimumScaleFactor=0.66
        counter.isHidden=true
        counter.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(counter);hud=counter
        NSLayoutConstraint.activate([counter.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:12),counter.leadingAnchor.constraint(equalTo:view.leadingAnchor,constant:14),counter.trailingAnchor.constraint(lessThanOrEqualTo:view.trailingAnchor,constant:-91),counter.heightAnchor.constraint(equalToConstant:46)])
        gameCanvas.onHUDUpdate = { [weak counter] live in
            counter?.text=" Level \(live.displayLevel) · \(BopaviCore.collectibleIcons[world]) \(live.stars+live.coins) "
        }
        let pause=UIButton(type:.system);pause.setTitle("Ⅱ",for:.normal);pause.titleLabel?.font=UIFont.boldSystemFont(ofSize:24)
        pause.backgroundColor=UIColor(red:0.11,green:0.24,blue:0.41,alpha:0.96);pause.setTitleColor(.white,for:.normal)
        pause.layer.cornerRadius=16;pause.layer.borderWidth=1
        pause.layer.borderColor=UIColor.white.withAlphaComponent(0.3).cgColor
        pause.accessibilityLabel="Izbornik tijekom igre"
        // Keep pause out of the idle flight preview and VoiceOver tree.
        pause.isHidden=true
        pause.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(pause)
        gameCanvas.onFlightStarted = { [weak self,weak gameCanvas,weak pause,weak counter] in
            guard let self=self, self.canvas === gameCanvas else{return}
            // The pre-flight preview never spends purchased equipment.
            _ = self.progress.consumePerks()
            pause?.isHidden=false
            counter?.isHidden=false
        }
        NSLayoutConstraint.activate([pause.trailingAnchor.constraint(equalTo:view.trailingAnchor,constant:-15),pause.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:12),pause.heightAnchor.constraint(equalToConstant:48),pause.widthAnchor.constraint(equalToConstant:58)])
        pause.addAction(UIAction{[weak self,weak gameCanvas] _ in
            guard let self=self,let canvas=gameCanvas,
                  self.canvas === canvas,canvas.game.active && !canvas.game.finished else{return}
            canvas.paused=true;self.sound.pause()
            let dialog=UIAlertController(title:"Pauza · Level \(canvas.game.displayLevel)",message:nil,preferredStyle:.actionSheet)
            dialog.addAction(UIAlertAction(title:"Nastavi let",style:.default){_ in canvas.paused=false;self.sound.resume()})
            dialog.addAction(UIAlertAction(title:self.progress.soundEnabled ? "🔇 Isključi zvuk" : "🔊 Uključi zvuk",style:.default){_ in
                let enabled = !self.progress.soundEnabled
                self.progress.soundEnabled = enabled
                self.sound.enabled = enabled
                canvas.paused = false
                if enabled {self.sound.resume()}
            })
            dialog.addAction(UIAlertAction(title:"Mapa svjetova",style:.default){_ in self.showWorlds()})
            dialog.addAction(UIAlertAction(title:"Oprema za kovanice",style:.default){_ in self.showPerks()})
            dialog.addAction(UIAlertAction(title:"Izgled Bopija",style:.default){_ in self.showSkins()})
            dialog.addAction(UIAlertAction(title:"Zvuk i prikaz",style:.default){_ in self.showSettings()})
            dialog.addAction(UIAlertAction(title:"Odustani",style:.cancel){_ in canvas.paused=false;self.sound.resume()})
            dialog.popoverPresentationController?.sourceView=pause
            dialog.popoverPresentationController?.sourceRect=pause.bounds
            self.present(dialog,animated:true)
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
        sound.effect("hit")
        let headline=ResultHeadline.label(score:g.score(),previousBest:progress.bestPoints(),won:g.won)
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
        let header=UIImageView(image:UIImage(named:"Hero"))
        header.contentMode = .scaleAspectFill;header.clipsToBounds=true
        header.layer.cornerRadius=22;header.layer.borderWidth=2
        header.layer.borderColor=UIColor.white.withAlphaComponent(0.45).cgColor
        header.heightAnchor.constraint(equalToConstant:166).isActive=true
        stack.addArrangedSubview(header)
        label(headline,31,UIColor(red:1,green:0.86,blue:0.38,alpha:1),stack)
        let stats=UIStackView()
        stats.axis = .vertical;stats.alignment = .fill;stats.spacing=8
        stats.isLayoutMarginsRelativeArrangement=true
        stats.layoutMargins=UIEdgeInsets(top:16,left:18,bottom:18,right:18)
        stats.backgroundColor=UIColor(red:0.02,green:0.13,blue:0.31,alpha:0.97)
        stats.layer.cornerRadius=22;stats.layer.borderWidth=2
        stats.layer.borderColor=UIColor(red:0.23,green:0.78,blue:1,alpha:1).cgColor
        stack.addArrangedSubview(stats)
        label("\(g.score()) BODOVA",37,UIColor(red:1,green:0.81,blue:0.29,alpha:1),stats)
        label("🏆  Najbolji rezultat: \(progress.bestPoints())",16,
              UIColor.white,stats)
        label("Level \(gameNumber) · Ukupno prolaza: \(g.totalPassed)",16,
              UIColor.white,stats)
        label("Težina: \(progress.difficultyNames[g.difficulty]) · \(progress.playerName)",16,
              UIColor.white,stats)
        label("\(BopaviCore.collectibleIcons[gameWorld]) \(g.coins+g.stars)  ·  ● \(progress.coins()) kovanica",16,
              UIColor.white,stats)
        sectionHeading("✦  NAGRADE I NAPREDAK",in:stack)
        label("●  Osvojeno u letu: \(g.coins+g.stars) · Ukupno: \(progress.coins()) kovanica",16,.white,stack)
        button("▶  PONOVO",in:stack){self.showPilotPicker(self.gameWorld,self.gameNumber)}
        button("LOKALNA LJESTVICA",in:stack,primary:false){self.showLeaderboard()}
        button("🛍  TRGOVINA KOVANICAMA",in:stack,primary:false){self.showPerks()}
        button("MAPA SVJETOVA",in:stack,primary:false){self.showWorlds()}
        button("POČETNI EKRAN",in:stack,primary:false){self.showHome()}
    }
    private func showPerks(){
        let s=menu("TRGOVINA","Za kovanice osvojene igrom — bez stvarnog novca")
        label("●  \(progress.coins()) KOVANICA",24,UIColor(red:1,green:0.86,blue:0.44,alpha:1),s)
        label("Za svakih novih 1.000 bodova najboljeg rezultata dobivaš 1 kovanicu.",15,.white,s)
        let bonus=progress.bonusCoinsAvailable()
        if bonus>0 {
            button("🎁  PREUZMI \(bonus) KOVANICA ZA BODOVE",in:s,primary:false){
                let earned=self.progress.claimBonusCoins()
                if earned>0 {self.sound.effect("purchase");self.showPerks()}
            }
        }

        label("Osvajaj kovanice prelaskom nagradnih levela i biraj opremu za sljedeći let.",15,.white,s)
        for n in 0..<2 {
            let extra=n==0 ? "Čuva Bopija od jednog sudara" : "Privlači kovanice i predmete 8 sekundi"
            label("\(n==0 ? "🛡 ŠTIT" : "🧲 MAGNET") · \(extra) · U torbi: \(progress.perkCount(n))",16,.white,s)
            button("KUPI \(n==0 ? "ŠTIT" : "MAGNET") · \(progress.perkPrices[n]) KOVANICA",in:s,primary:false){
                if self.progress.buyPerk(n){self.sound.effect("purchase");self.showPerks()}
                else {self.alert("Kupnja nije moguća","Nedovoljno kovanica ili je zaliha puna.")}
            }
        }
        label("Kupljena oprema automatski se koristi na početku sljedećeg leta.",14,.white,s)
        button("🎨  LIKOVI",in:s,primary:false){self.showSkins()}
        button("‹  POSTAVKE",in:s,primary:false){self.showSettings()}
    }
    private func showSkins(){
        let s=menu("LIKOVI","Devet letača, uključujući Nou i Any")
        label("Stanje: \(progress.coins()) kovanica",20,.white,s)
        characterGallery(in:s){self.showSkins()}
        button("‹  POSTAVKE",in:s,primary:false){self.showSettings()}
    }
    private func showLeaderboard(){
        let s=menu("LJESTVICA","Najbolji stvarni rezultati na ovom uređaju")
        let entries=progress.leaderboard()
        if entries.isEmpty {label("Još nema rezultata. Odigraj let i osvoji bodove!",16,.white,s)}
        for (index,item) in entries.enumerated() {
            label("\(index+1). \(item.name) · \(item.score) bodova",18,.white,s)
            label("\(BopaviCore.names[item.world]) · \(progress.difficultyNames[item.difficulty]) · \(item.gates) prolaza",14,.white,s)
        }
        button("POSTIGNUĆA",in:s,primary:false){self.showAchievements()}
        button("‹  POSTAVKE",in:s,primary:false){self.showSettings()}
    }
    private func showAchievements(){
        let s=menu("POSTIGNUĆA","Tvoj napredak spremljen je samo na uređaju")
        let total=(0..<8).reduce(0){$0+self.progress.frontier($1)-1}
        label("Dovršeni leveli: \(total)",19,.white,s)
        label("Pobjede: \(progress.wins()) · Pokušaji bez pobjede: \(progress.deaths())",17,.white,s)
        label("Napredak je spremljen samo na ovom uređaju.",17,.white,s)
        for w in 0..<8 {label("\(BopaviCore.names[w]) · najbolji rezultat \(progress.best(w))",16,.white,s)}
        button("‹  POSTAVKE",in:s,primary:false){self.showSettings()}
    }
    private func showSettings(){
        let s=menu("POSTAVKE","Prilagodi igru svom stilu")
        sectionHeading("IZGLED I ZVUK",in:s)
        let toggle=UISwitch();toggle.isOn=progress.lessMotion
        toggle.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        let toggleRow=UIStackView();toggleRow.axis = .horizontal;toggleRow.spacing=12
        let l=UILabel();l.text="Nježnije animacije";l.font=UIFont.systemFont(ofSize:16,weight:.medium);l.textColor = .white
        l.numberOfLines=0;l.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        toggleRow.addArrangedSubview(l);toggleRow.addArrangedSubview(toggle);s.addArrangedSubview(toggleRow)
        toggle.addAction(UIAction{_ in self.progress.lessMotion=toggle.isOn},for:.valueChanged)
        let audio=UISwitch();audio.isOn=progress.soundEnabled
        audio.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        let audioRow=UIStackView();audioRow.axis = .horizontal;audioRow.spacing=12
        let audioLabel=UILabel();audioLabel.text="Glazba i zvučni efekti";audioLabel.font=UIFont.systemFont(ofSize:16,weight:.medium);audioLabel.textColor = .white
        audioLabel.numberOfLines=0;audioLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        audioRow.addArrangedSubview(audioLabel);audioRow.addArrangedSubview(audio);s.addArrangedSubview(audioRow)
        audio.addAction(UIAction{_ in self.progress.soundEnabled=audio.isOn;self.sound.enabled=audio.isOn},for:.valueChanged)
        let haptic=UISwitch();haptic.isOn=progress.hapticEnabled
        haptic.onTintColor=UIColor(red:0.05,green:0.62,blue:0.98,alpha:1)
        let hapticRow=UIStackView();hapticRow.axis = .horizontal;hapticRow.spacing=12
        let hapticLabel=UILabel();hapticLabel.text="Vibracije pri igranju"
        hapticLabel.font=UIFont.systemFont(ofSize:16,weight:.medium)
        hapticLabel.textColor = .white;hapticLabel.numberOfLines=0
        hapticLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        hapticRow.addArrangedSubview(hapticLabel);hapticRow.addArrangedSubview(haptic)
        s.addArrangedSubview(hapticRow)
        haptic.addAction(UIAction{_ in self.progress.hapticEnabled=haptic.isOn},for:.valueChanged)
        sectionHeading("IGRAČ I TEŽINA",in:s)
        label("TEŽINA IGRE — utječe na brzinu i gravitaciju",16,.white,s)
        let difficulty=UISegmentedControl(items:["Lako","Normalno","Teško"])
        difficulty.selectedSegmentIndex=progress.difficulty
        difficulty.selectedSegmentTintColor=UIColor(red:1,green:0.72,blue:0.16,alpha:1)
        difficulty.backgroundColor=UIColor(red:0.03,green:0.16,blue:0.34,alpha:1)
        difficulty.setTitleTextAttributes([.foregroundColor:UIColor.white],for:.normal)
        difficulty.setTitleTextAttributes([.foregroundColor:UIColor(red:0.12,green:0.15,blue:0.24,alpha:1)],for:.selected)
        difficulty.heightAnchor.constraint(equalToConstant:44).isActive=true
        difficulty.addAction(UIAction{_ in self.progress.difficulty=difficulty.selectedSegmentIndex},for:.valueChanged)
        s.addArrangedSubview(difficulty)
        label("Težina se primjenjuje na sljedeći let. Dosadašnji napredak ostaje spremljen.",14,.white,s)
        let player=UITextField()
        player.text=progress.playerName
        player.placeholder="Tvoje ime"
        player.textColor = .white
        player.backgroundColor=UIColor(red:0.02,green:0.15,blue:0.32,alpha:1)
        player.layer.cornerRadius=13
        player.layer.borderWidth=1
        player.layer.borderColor=UIColor(red:0.30,green:0.76,blue:1,alpha:0.7).cgColor
        player.heightAnchor.constraint(equalToConstant:52).isActive=true
        s.addArrangedSubview(player)
        button("SPREMI IME",in:s,primary:false){
            player.resignFirstResponder()
            self.progress.playerName=player.text ?? ""
            self.alert("Spremljeno","Ime igrača spremljeno je samo na ovom uređaju.")
        }
        sectionHeading("DODATNE OPCIJE",in:s)
        button("🐤  LIKOVI",in:s,primary:false){self.showSkins()}
        button("🛍  TRGOVINA KOVANICAMA",in:s,primary:false){self.showPerks()}
        button("🏆  LOKALNA LJESTVICA",in:s,primary:false){self.showLeaderboard()}
        sectionHeading("PODACI I PRIVATNOST",in:s)
        label("BOPAVI — Mali let, velika avantura. Stvorio Brendigo.",14,.white,s)
        label("Bez oglasa i kupnje stvarnim novcem. Tvoj napredak ostaje na uređaju.",14,.white,s)
        button("SPREMI KOPIJU NAPRETKA",in:s,primary:false){
            do {
                let url=FileManager.default.temporaryDirectory.appendingPathComponent("bopavi-save.json")
                try self.progress.exportData().write(to:url,options:.atomic)
                let activity=UIActivityViewController(activityItems:[url],applicationActivities:nil)
                activity.popoverPresentationController?.sourceView=self.view
                self.present(activity,animated:true)
            } catch {self.alert("Izvoz nije uspio",error.localizedDescription)}
        }
        button("VRATI NAPREDAK IZ KOPIJE",in:s,primary:false){
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
            showHome();alert("Uspješno","Napredak je uvezen.")
        }
        catch {alert("Uvoz nije uspio",error.localizedDescription)}
    }
    private func alert(_ title:String,_ message:String){let a=UIAlertController(title:title,message:message,preferredStyle:.alert);a.addAction(UIAlertAction(title:"U redu",style:.default));present(a,animated:true)}
}
