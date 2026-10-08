import UIKit
import UniformTypeIdentifiers

/// Premium, self-contained control. The gradient resizes without a new image allocation.
private final class BopaviActionButton: UIButton {
    private let gradient = CAGradientLayer()
    init(primary:Bool) {
        super.init(frame:.zero)
        gradient.colors = primary
            ? [UIColor(red:0.77,green:1.00,blue:0.49,alpha:1).cgColor,
               UIColor(red:0.40,green:0.91,blue:0.27,alpha:1).cgColor,
               UIColor(red:0.07,green:0.72,blue:0.26,alpha:1).cgColor]
            : [UIColor(red:0.14,green:0.55,blue:0.95,alpha:1).cgColor,
               UIColor(red:0.07,green:0.22,blue:0.62,alpha:1).cgColor]
        gradient.startPoint=CGPoint(x:0.5,y:0);gradient.endPoint=CGPoint(x:0.5,y:1)
        layer.insertSublayer(gradient,at:0)
        layer.cornerRadius=19
        layer.borderWidth=primary ? 2 : 1
        layer.borderColor=UIColor.white.withAlphaComponent(primary ? 0.72 : 0.24).cgColor
        layer.shadowColor=UIColor.black.cgColor
        layer.shadowOpacity=0.23
        layer.shadowRadius=6
        layer.shadowOffset=CGSize(width:0,height:4)
        setTitleColor(.white,for:.normal)
        titleLabel?.font=UIFont.systemFont(ofSize:17,weight:.heavy)
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
        gradient.cornerRadius=19
        layer.shadowPath=UIBezierPath(roundedRect:bounds,cornerRadius:19).cgPath
    }
}

/** Native UIKit menus and Core Graphics gameplay, no WKWebView. */
final class GameController: UIViewController, UIDocumentPickerDelegate {
    private let progress=ProgressStore()
    private let sound=Soundscape()
    private let collectHaptic=UISelectionFeedbackGenerator()
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
    override func viewDidLoad(){super.viewDidLoad();sound.enabled=progress.soundEnabled;showHome()}
    private func clear() {
        canvas?.stop();canvas=nil;hud=nil;sound.stop()
        setNeedsStatusBarAppearanceUpdate()
        backgroundGradient=nil
        view.subviews.forEach{$0.removeFromSuperview()}
        view.layer.sublayers?.forEach{$0.removeFromSuperlayer()}
        view.backgroundColor=UIColor(red:0.03,green:0.10,blue:0.24,alpha:1)
    }
    private func menu(_ title:String,_ subtitle:String)->UIStackView {
        clear()
        let gradient=CAGradientLayer()
        gradient.colors=[UIColor(red:0.04,green:0.26,blue:0.48,alpha:1).cgColor,
                         UIColor(red:0.04,green:0.42,blue:0.69,alpha:1).cgColor,
                         UIColor(red:0.17,green:0.62,blue:0.85,alpha:1).cgColor]
        gradient.frame=view.bounds
        view.layer.insertSublayer(gradient,at:0);backgroundGradient=gradient
        let scroll=UIScrollView();scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.alwaysBounceVertical=true
        scroll.showsVerticalScrollIndicator=false
        scroll.keyboardDismissMode = .interactive
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.leadingAnchor),scroll.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        let stack=UIStackView();stack.axis = .vertical;stack.alignment = .fill;stack.spacing=12
        stack.translatesAutoresizingMaskIntoConstraints=false
        stack.layoutMargins=UIEdgeInsets(top:22,left:20,bottom:32,right:20)
        stack.isLayoutMarginsRelativeArrangement=true
        scroll.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            stack.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            stack.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            stack.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor)])
        if title != "BOPAVI" {
            label(title,34,UIColor(red:1,green:0.82,blue:0.47,alpha:1),stack)
            if !subtitle.isEmpty {label(subtitle,15,UIColor(red:0.74,green:0.87,blue:0.96,alpha:1),stack)}
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
    private func button(_ title:String,in stack:UIStackView,primary:Bool=true,action:@escaping()->Void){
        let b=BopaviActionButton(primary:primary)
        b.setTitle(title,for:.normal)
        b.accessibilityLabel=title
        b.heightAnchor.constraint(greaterThanOrEqualToConstant:58).isActive=true
        b.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        stack.addArrangedSubview(b)
    }
    private func worldTile(_ world:Int,in stack:UIStackView,action:@escaping()->Void){
        let unlocked=world<=progress.maxWorld()
        let tile=BopaviActionButton(primary:false)
        tile.layer.borderColor=worldAccents[world].withAlphaComponent(0.65).cgColor
        if let illustration=UIImage(named:"World\(world)") {
            let preview=UIImageView(image:illustration)
            preview.translatesAutoresizingMaskIntoConstraints=false
            preview.contentMode = .scaleAspectFill
            preview.clipsToBounds=true
            preview.alpha=0.76
            preview.isUserInteractionEnabled=false
            tile.insertSubview(preview,at:0)
            NSLayoutConstraint.activate([
                preview.leadingAnchor.constraint(equalTo:tile.leadingAnchor),
                preview.trailingAnchor.constraint(equalTo:tile.trailingAnchor),
                preview.topAnchor.constraint(equalTo:tile.topAnchor),
                preview.bottomAnchor.constraint(equalTo:tile.bottomAnchor)
            ])
            tile.clipsToBounds=true
        }
        tile.alpha=unlocked ? 1 : 0.60
        tile.titleLabel?.numberOfLines=3
        tile.titleLabel?.textAlignment = .center
        let headline="\(BopaviCore.collectibleIcons[world])  \(BopaviCore.names[world])  \(unlocked ? "↗" : "🔒")"
        let detail=unlocked ? "Level \(progress.streamFrontier(world)) · \(BopaviCore.collectibles[world])" : "Otkrij novi svijet tijekom igranja"
        let text=NSMutableAttributedString(string:headline+"\n"+detail)
        text.addAttributes([.font:UIFont.systemFont(ofSize:15,weight:.heavy),.foregroundColor:worldAccents[world]],range:NSRange(location:0,length:(headline as NSString).length))
        text.addAttributes([.font:UIFont.systemFont(ofSize:11,weight:.medium),.foregroundColor:UIColor(red:0.78,green:0.90,blue:0.96,alpha:1)],range:NSRange(location:(headline as NSString).length+1,length:(detail as NSString).length))
        tile.setAttributedTitle(text,for:.normal)
        tile.accessibilityLabel="\(BopaviCore.names[world]), \(unlocked ? "otključano" : "zaključano")"
        tile.heightAnchor.constraint(equalToConstant:178).isActive=true
        tile.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        stack.addArrangedSubview(tile)
    }
    private func showHome(){
        let stack=menu("BOPAVI","")
        // Full-bleed hero artwork and transparent controls match the bright reference.
        backgroundGradient?.removeFromSuperlayer()
        backgroundGradient=nil
        view.backgroundColor=UIColor(red:0.17,green:0.65,blue:0.94,alpha:1)
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
        stack.heightAnchor.constraint(greaterThanOrEqualTo:view.safeAreaLayoutGuide.heightAnchor).isActive=true
        // LaunchArt is portrait and already includes the BOPAVI logo and slogan.
        let wallet=UIView()
        wallet.backgroundColor=UIColor(red:0.06,green:0.19,blue:0.37,alpha:0.9)
        wallet.layer.cornerRadius=20
        wallet.layer.borderWidth=1
        wallet.layer.borderColor=UIColor.white.withAlphaComponent(0.35).cgColor
        wallet.heightAnchor.constraint(equalToConstant:52).isActive=true
        let amount=UILabel()
        amount.translatesAutoresizingMaskIntoConstraints=false
        amount.text="●  \(progress.coins()) kovanica"
        amount.textAlignment = .center
        amount.font=UIFont.systemFont(ofSize:18,weight:.heavy)
        amount.textColor=UIColor(red:1,green:0.86,blue:0.44,alpha:1)
        wallet.addSubview(amount)
        NSLayoutConstraint.activate([
            amount.leadingAnchor.constraint(equalTo:wallet.leadingAnchor,constant:12),
            amount.trailingAnchor.constraint(equalTo:wallet.trailingAnchor,constant:-12),
            amount.centerYAnchor.constraint(equalTo:wallet.centerYAnchor)
        ])
        // Compact top-right counter, while the primary CTA remains at the bottom.
        let walletRow=UIStackView()
        walletRow.axis = .horizontal;walletRow.alignment = .center
        walletRow.addArrangedSubview(UIView())
        wallet.widthAnchor.constraint(equalToConstant:175).isActive=true
        wallet.heightAnchor.constraint(equalToConstant:46).isActive=true
        walletRow.addArrangedSubview(wallet)
        stack.addArrangedSubview(walletRow)
        let spacer=UIView()
        spacer.setContentHuggingPriority(.defaultLow,for:.vertical)
        stack.addArrangedSubview(spacer)
        button("▶  IGRAJ",in:stack){
            let world=self.progress.chosenWorld()
            self.startGame(world,self.progress.streamFrontier(world))
        }
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
                let accessible=world<=progress.maxWorld()
                worldTile(world,in:row){
                    if accessible {self.showLevels(world,page:1)}
                    else {self.alert("Svijet je zaključan","Dovrši 30 levela prethodnog svijeta.")}
                }
            }
        }
        button("‹  Natrag",in:stack,primary:false){self.showHome()}
    }
    private func showLevels(_ world:Int,page:Int){
        gameWorld=world;progress.chooseWorld(world)
        let s=menu(BopaviCore.names[world],"Odaberi otključani level")
        let maxNumber=min(BopaviCore.levelsPerWorld,progress.frontier(world))
        label("Otključano do levela \(maxNumber)",18,.white,s)
        button("▶  NASTAVI LET",in:s){self.startGame(world,self.progress.streamFrontier(world))}
        let field=UITextField()
        field.keyboardType = .numberPad;field.text=String(page);field.placeholder="Broj levela"
        field.textAlignment = .center;field.textColor = .white;field.backgroundColor = UIColor(red:0.10,green:0.24,blue:0.45,alpha:1)
        field.layer.cornerRadius=12;field.heightAnchor.constraint(equalToConstant:52).isActive=true
        s.addArrangedSubview(field)
        button("▶  POKRENI ODABRANI LEVEL",in:s){
            field.resignFirstResponder()
            guard let n=Int(field.text ?? ""), (1...maxNumber).contains(n) else {self.alert("Nedostupan level","Odaberi otključan level iz raspona 1–\(maxNumber).");return}
            self.startGame(world,n)
        }
        let start=((max(1,min(page,BopaviCore.levelsPerWorld))-1)/20)*20+1
        for rowNumber in 0..<5 {
            let row=UIStackView();row.axis = .horizontal;row.spacing=6
            row.distribution = .fillEqually
            for column in 0..<4 {
                let n=start+rowNumber*4+column
                if n>BopaviCore.levelsPerWorld {break}
                let unlocked=n<=maxNumber
                let cell=BopaviActionButton(primary:false)
                cell.setTitle("\(unlocked ? (n<maxNumber ? "★" : "▶") : "🔒")\n\(n)",for:.normal)
                cell.alpha=unlocked ? 1 : 0.60
                cell.titleLabel?.numberOfLines=2
                cell.titleLabel?.textAlignment = .center
                cell.titleLabel?.font=UIFont.monospacedDigitSystemFont(ofSize:14,weight:.bold)
                cell.accessibilityLabel="Level \(n)"
                cell.heightAnchor.constraint(equalToConstant:63).isActive=true
                cell.addAction(UIAction{_ in
                    self.sound.effect("click")
                    if unlocked {self.startGame(world,n)}
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
    private func startGame(_ world:Int,_ number:Int){
        clear();gameWorld=world;gameNumber=number
        let boosts=progress.consumePerks()
        let game=GameSimulation(BopaviCore.createStream(world,number),endless:true,initialShield:boosts.shield,initialMagnet:boosts.magnet,initialOrdinal:number)
        sound.startWorld(world)
        let gameCanvas=GameCanvas(game:game,reducedMotion:progress.lessMotion,skinIndex:progress.skinIndex())
        canvas=gameCanvas
        setNeedsStatusBarAppearanceUpdate()
        gameCanvas.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(gameCanvas)
        NSLayoutConstraint.activate([gameCanvas.topAnchor.constraint(equalTo:view.topAnchor),gameCanvas.bottomAnchor.constraint(equalTo:view.bottomAnchor),gameCanvas.leadingAnchor.constraint(equalTo:view.leadingAnchor),gameCanvas.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        gameCanvas.onFinished = { [weak self] g in self?.showResult(g) }
        gameCanvas.onFlap = { [weak self] in self?.sound.effect("tap") }
        gameCanvas.onCollect = { [weak self] in
            self?.sound.effect("collect")
            self?.collectHaptic.selectionChanged()
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
        counter.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(counter);hud=counter
        NSLayoutConstraint.activate([counter.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:12),counter.leadingAnchor.constraint(equalTo:view.leadingAnchor,constant:14),counter.trailingAnchor.constraint(lessThanOrEqualTo:view.trailingAnchor,constant:-91),counter.heightAnchor.constraint(equalToConstant:46)])
        gameCanvas.onHUDUpdate = { [weak counter] live in
            counter?.text=" Level \(live.displayLevel) · \(BopaviCore.collectibleIcons[world]) \(live.stars+live.coins) "
        }
        let pause=UIButton(type:.system);pause.setTitle("Ⅱ",for:.normal);pause.titleLabel?.font=UIFont.boldSystemFont(ofSize:24)
        pause.backgroundColor=UIColor(red:0.11,green:0.24,blue:0.41,alpha:0.96);pause.setTitleColor(.white,for:.normal)
        pause.layer.cornerRadius=16;pause.layer.borderWidth=1
        pause.layer.borderColor=UIColor.white.withAlphaComponent(0.3).cgColor
        pause.accessibilityLabel="Izbornik tijekom igre";pause.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(pause)
        NSLayoutConstraint.activate([pause.trailingAnchor.constraint(equalTo:view.trailingAnchor,constant:-15),pause.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:12),pause.heightAnchor.constraint(equalToConstant:48),pause.widthAnchor.constraint(equalToConstant:58)])
        pause.addAction(UIAction{[weak self,weak gameCanvas] _ in
            guard let self=self,let canvas=gameCanvas else{return}
            canvas.paused=true;self.sound.pause()
            let dialog=UIAlertController(title:"Pauza",message:nil,preferredStyle:.actionSheet)
            dialog.addAction(UIAlertAction(title:"Nastavi let",style:.default){_ in canvas.paused=false;self.sound.resume()})
            dialog.addAction(UIAlertAction(title:"Mapa svjetova",style:.default){_ in self.showWorlds()})
            dialog.addAction(UIAlertAction(title:"Oprema za kovanice",style:.default){_ in self.showPerks()})
            dialog.addAction(UIAlertAction(title:"Izgled Bopija",style:.default){_ in self.showSkins()})
            dialog.addAction(UIAlertAction(title:"Zvuk i prikaz",style:.default){_ in self.showSettings()})
            dialog.addAction(UIAlertAction(title:"Odustani",style:.cancel){_ in canvas.paused=false;self.sound.resume()})
            dialog.popoverPresentationController?.sourceView=pause
            dialog.popoverPresentationController?.sourceRect=pause.bounds
            self.present(dialog,animated:true)
        },for:.touchUpInside)
        NotificationCenter.default.removeObserver(self,name:UIApplication.willResignActiveNotification,object:nil)
        NotificationCenter.default.addObserver(self,selector:#selector(backgroundPause),name:UIApplication.willResignActiveNotification,object:nil)
    }
    @objc private func backgroundPause(){canvas?.paused=true;sound.pause()}
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
        dim.backgroundColor=UIColor(red:0.01,green:0.09,blue:0.23,alpha:0.67)
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
        label("LET ZAVRŠEN!",31,UIColor(red:1,green:0.86,blue:0.38,alpha:1),stack)
        let stats=UIStackView()
        stats.axis = .vertical;stats.alignment = .fill;stats.spacing=8
        stats.isLayoutMarginsRelativeArrangement=true
        stats.layoutMargins=UIEdgeInsets(top:16,left:18,bottom:18,right:18)
        stats.backgroundColor=UIColor(red:0.96,green:0.98,blue:1,alpha:1)
        stats.layer.cornerRadius=22;stats.layer.borderWidth=2
        stats.layer.borderColor=UIColor(red:1,green:0.83,blue:0.40,alpha:1).cgColor
        stack.addArrangedSubview(stats)
        label("\(g.score())",43,UIColor(red:0.05,green:0.22,blue:0.48,alpha:1),stats)
        label("Level \(gameNumber) · Prolazi \(g.passed)/\(g.level.gates.count)",16,
              UIColor(red:0.09,green:0.28,blue:0.50,alpha:1),stats)
        label("\(BopaviCore.collectibleIcons[gameWorld]) \(g.coins+g.stars)  ·  ● \(progress.coins()) kovanica",16,
              UIColor(red:0.09,green:0.28,blue:0.50,alpha:1),stats)
        button("▶  PONOVO",in:stack){self.startGame(self.gameWorld,self.gameNumber)}
        button("OPREMA ZA KOVANICE",in:stack,primary:false){self.showPerks()}
        button("MAPA SVJETOVA",in:stack,primary:false){self.showWorlds()}
        button("POČETNI EKRAN",in:stack,primary:false){self.showHome()}
    }
    private func showPerks(){
        let s=menu("OPREMA","Pogodnosti kupuješ samo osvojenim kovanicama")
        label("Tvoje kovanice: \(progress.coins())",20,.white,s)
        label("Kovanice osvajaš prvim prelaskom nagradnih levela.",15,.white,s)
        for n in 0..<2 {
            let extra=n==0 ? "Štiti od jednog udarca" : "Privlači predmete osam sekundi"
            label("\(progress.perkNames[n]) · \(extra) · u zalihi \(progress.perkCount(n))",16,.white,s)
            button("KUPI ZA \(progress.perkPrices[n]) KOVANICA",in:s,primary:false){
                if self.progress.buyPerk(n){self.sound.effect("purchase");self.showPerks()}
                else {self.alert("Kupnja nije moguća","Nedovoljno kovanica ili je zaliha puna.")}
            }
        }
        label("Kupljene pogodnosti aktiviraju se pri sljedećem letu.",14,.white,s)
        button("‹  Mapa svjetova",in:s,primary:false){self.showWorlds()}
    }
    private func showSkins(){
        let s=menu("LIKOVI","Skupljaj kovanice i otključaj nove Bopijeve boje")
        label("Stanje: \(progress.coins()) kovanica",20,.white,s)
        for i in 0..<6 {
            let description=progress.skinIndex()==i ? "✓ ODABRAN" : progress.owned(i) ? "OTKLJUČAN" : "\(progress.costs[i]) kovanica"
            button("🐦 \(progress.skinNames[i]) · \(description)",in:s,primary:progress.skinIndex()==i){
                if !self.progress.selectOrBuy(i){self.alert("Nedovoljno kovanica","Skupljaj kovanice tijekom leta.")}
                else {self.showSkins()}
            }
        }
        button("‹  Mapa svjetova",in:s,primary:false){self.showWorlds()}
    }
    private func showAchievements(){
        let s=menu("POSTIGNUĆA","Tvoj napredak spremljen je samo na uređaju")
        let total=(0..<8).reduce(0){$0+self.progress.frontier($1)-1}
        label("Dovršeni leveli: \(total)",19,.white,s)
        label("Pobjede: \(progress.wins()) · Pokušaji bez pobjede: \(progress.deaths())",17,.white,s)
        label("Napredak je spremljen samo na ovom uređaju.",17,.white,s)
        for w in 0..<8 {label("\(BopaviCore.names[w]) · najbolji rezultat \(progress.best(w))",16,.white,s)}
        button("‹  Mapa svjetova",in:s,primary:false){self.showWorlds()}
    }
    private func showSettings(){
        let s=menu("POSTAVKE","Privatnost, animacije i sigurnosna kopija")
        let toggle=UISwitch();toggle.isOn=progress.lessMotion
        let toggleRow=UIStackView();toggleRow.axis = .horizontal;toggleRow.spacing=12
        let l=UILabel();l.text="Smanji animacije (30 FPS)";l.font=UIFont.systemFont(ofSize:16,weight:.medium);l.textColor = .white
        l.numberOfLines=0;l.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        toggleRow.addArrangedSubview(l);toggleRow.addArrangedSubview(toggle);s.addArrangedSubview(toggleRow)
        toggle.addAction(UIAction{_ in self.progress.lessMotion=toggle.isOn},for:.valueChanged)
        let audio=UISwitch();audio.isOn=progress.soundEnabled
        let audioRow=UIStackView();audioRow.axis = .horizontal;audioRow.spacing=12
        let audioLabel=UILabel();audioLabel.text="Glazba i zvučni efekti";audioLabel.font=UIFont.systemFont(ofSize:16,weight:.medium);audioLabel.textColor = .white
        audioLabel.numberOfLines=0;audioLabel.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
        audioRow.addArrangedSubview(audioLabel);audioRow.addArrangedSubview(audio);s.addArrangedSubview(audioRow)
        audio.addAction(UIAction{_ in self.progress.soundEnabled=audio.isOn;self.sound.enabled=audio.isOn},for:.valueChanged)
        label("Bez oglasa, telemetrije, računa i mrežnih zahtjeva.",14,.white,s)
        button("IZVEZI NAPREDAK",in:s,primary:false){
            do {
                let url=FileManager.default.temporaryDirectory.appendingPathComponent("bopavi-save.json")
                try self.progress.exportData().write(to:url,options:.atomic)
                let activity=UIActivityViewController(activityItems:[url],applicationActivities:nil)
                activity.popoverPresentationController?.sourceView=self.view
                self.present(activity,animated:true)
            } catch {self.alert("Izvoz nije uspio",error.localizedDescription)}
        }
        button("UVEZI NAPREDAK (v0.1–v0.5)",in:s,primary:false){
            let picker=UIDocumentPickerViewController(forOpeningContentTypes:[.json],asCopy:true)
            picker.delegate=self;self.present(picker,animated:true)
        }
        button("‹  Mapa svjetova",in:s,primary:false){self.showWorlds()}
    }
    func documentPicker(_ controller:UIDocumentPickerViewController,didPickDocumentsAt urls:[URL]) {
        guard let url=urls.first else{return}
        let scoped=url.startAccessingSecurityScopedResource()
        defer{if scoped {url.stopAccessingSecurityScopedResource()}}
        do {try progress.importData(Data(contentsOf:url));sound.enabled=progress.soundEnabled;showHome();alert("Uspješno","Napredak je uvezen.")}
        catch {alert("Uvoz nije uspio",error.localizedDescription)}
    }
    private func alert(_ title:String,_ message:String){let a=UIAlertController(title:title,message:message,preferredStyle:.alert);a.addAction(UIAlertAction(title:"U redu",style:.default));present(a,animated:true)}
}
