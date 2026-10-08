import UIKit
import UniformTypeIdentifiers

/** Native UIKit menus and Core Graphics gameplay, no WKWebView. */
final class GameController: UIViewController, UIDocumentPickerDelegate {
    private let progress=ProgressStore()
    private let sound=Soundscape()
    private var canvas:GameCanvas?
    private var gameWorld=0
    private var gameNumber=1
    private var hud:UILabel?
    private var backgroundGradient:CAGradientLayer?
    override var preferredStatusBarStyle:UIStatusBarStyle {.lightContent}
    override func viewDidLoad(){super.viewDidLoad();sound.enabled=progress.soundEnabled;showHome()}
    private func clear() {
        canvas?.stop();canvas=nil;hud=nil;sound.stop()
        backgroundGradient=nil
        view.subviews.forEach{$0.removeFromSuperview()}
        view.layer.sublayers?.forEach{$0.removeFromSuperlayer()}
        view.backgroundColor=UIColor(red:0.03,green:0.10,blue:0.24,alpha:1)
    }
    private func menu(_ title:String,_ subtitle:String)->UIStackView {
        clear()
        let gradient=CAGradientLayer()
        gradient.colors=[UIColor(red:0.025,green:0.075,blue:0.20,alpha:1).cgColor,
                         UIColor(red:0.07,green:0.27,blue:0.52,alpha:1).cgColor,
                         UIColor(red:0.06,green:0.54,blue:0.73,alpha:1).cgColor]
        gradient.frame=view.bounds
        view.layer.insertSublayer(gradient,at:0);backgroundGradient=gradient
        let scroll=UIScrollView();scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.alwaysBounceVertical=true;view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.leadingAnchor),scroll.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        let stack=UIStackView();stack.axis = .vertical;stack.alignment = .fill;stack.spacing=12
        stack.translatesAutoresizingMaskIntoConstraints=false;stack.layoutMargins=UIEdgeInsets(top:16,left:18,bottom:28,right:18);stack.isLayoutMarginsRelativeArrangement=true
        scroll.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            stack.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            stack.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            stack.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor)])
        label(title,34,UIColor(red:1,green:0.78,blue:0.28,alpha:1),stack)
        label(subtitle,15,UIColor(red:0.77,green:0.91,blue:1,alpha:1),stack)
        return stack
    }
    override func viewDidLayoutSubviews(){super.viewDidLayoutSubviews();backgroundGradient?.frame=view.bounds}
    @discardableResult private func label(_ text:String,_ size:CGFloat,_ color:UIColor,_ into:UIStackView)->UILabel {
        let l=UILabel();l.numberOfLines=0;l.textAlignment = .center;l.text=text;l.font=UIFont.systemFont(ofSize:size,weight:.heavy);l.textColor=color
        l.setContentCompressionResistancePriority(.required,for:.vertical);into.addArrangedSubview(l)
        return l
    }
    private func button(_ title:String,in stack:UIStackView,primary:Bool=true,action:@escaping()->Void){
        let b=UIButton(type:.system)
        b.setTitle(title,for:.normal);b.titleLabel?.font=UIFont.systemFont(ofSize:17,weight:.heavy)
        b.setTitleColor(.white,for:.normal)
        b.backgroundColor=primary ? UIColor(red:0.12,green:0.72,blue:0.32,alpha:1) : UIColor(red:0.08,green:0.50,blue:0.82,alpha:1)
        b.layer.cornerRadius=18;b.layer.borderWidth=1.5;b.layer.borderColor=UIColor.white.withAlphaComponent(0.35).cgColor
        b.heightAnchor.constraint(equalToConstant:54).isActive=true
        b.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        stack.addArrangedSubview(b)
    }
    private func showHome(){
        let s=menu("BOPAVI","Mali let, velika avantura")
        if let image=UIImage(named:"Hero") {
            let hero=UIImageView(image:image);hero.contentMode = .scaleAspectFit
            hero.heightAnchor.constraint(equalToConstant:180).isActive=true
            hero.isAccessibilityElement=true;hero.accessibilityLabel="Bopi, plava ptica s pilotskim naočalama"
            s.addArrangedSubview(hero)
        }
        label("Tapni, poleti i otkrivaj čudesne svjetove.",18,.white,s)
        label("● \(progress.coins()) kovanica",22,UIColor(red:1,green:0.86,blue:0.36,alpha:1),s)
        button("▶  IGRAJ",in:s){
            let w=self.progress.chosenWorld()
            self.startGame(w,self.progress.streamFrontier(w))
        }
        label("Bez oglasa i kupnje za stvarni novac",13,UIColor(white:0.87,alpha:1),s)
    }
    private func showWorlds(){
        let s=menu("SVJETOVI","Odaberi svoj sljedeći let")
        for w in 0..<8 {
            let accessible=w<=progress.maxWorld()
            let text="\(w+1). \(BopaviCore.names[w])  \(accessible ? "• \(progress.streamFrontier(w)-1) riješeno" : "🔒")"
            button(text,in:s,primary:accessible){
                if accessible {self.showLevels(w,page:1)} else {self.alert("Svijet je zaključan","Dovrši 30 levela prethodnog svijeta.")}
            }
        }
        button("‹  Natrag",in:s,primary:false){self.showHome()}
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
        for n in start...min(start+19,BopaviCore.levelsPerWorld){
            let open=n<=maxNumber
            button("\(open ? "✦" : "🔒")  Level \(n)  ·  Zona \(BopaviCore.create(world,n).zone)",in:s,primary:false){
                if open {self.startGame(world,n)} else {self.alert("Zaključano","Najprije dovrši prethodni level.")}
            }
        }
        button("← Prethodnih 20",in:s,primary:false){self.showLevels(world,page:max(1,start-20))}
        button("Sljedećih 20 →",in:s,primary:false){self.showLevels(world,page:min(BopaviCore.levelsPerWorld,start+20))}
        button("‹  Svjetovi",in:s,primary:false){self.showWorlds()}
    }
    private func startGame(_ world:Int,_ number:Int){
        clear();gameWorld=world;gameNumber=number
        let boosts=progress.consumePerks()
        let game=GameSimulation(BopaviCore.createStream(world,number),endless:true,initialShield:boosts.shield,initialMagnet:boosts.magnet,initialOrdinal:number)
        sound.startWorld(world)
        let gameCanvas=GameCanvas(game:game,reducedMotion:progress.lessMotion,skinIndex:progress.skinIndex())
        canvas=gameCanvas
        gameCanvas.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(gameCanvas)
        NSLayoutConstraint.activate([gameCanvas.topAnchor.constraint(equalTo:view.topAnchor),gameCanvas.bottomAnchor.constraint(equalTo:view.bottomAnchor),gameCanvas.leadingAnchor.constraint(equalTo:view.leadingAnchor),gameCanvas.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        gameCanvas.onFinished = { [weak self] g in self?.showResult(g) }
        gameCanvas.onFlap = { [weak self] in self?.sound.effect("tap") }
        gameCanvas.onCollect = { [weak self] in self?.sound.effect("collect") }
        gameCanvas.onLevelComplete = { [weak self] level in
            guard let self=self else{return}
            let reward=self.progress.completeLevel(world:world,number:level,score:game.score())
            self.sound.effect("level")
            if reward>0 {self.showToast("Level \(level): +\(reward) kovanica")}
        }
        let counter=UILabel();counter.text=" Level \(number) · \(BopaviCore.collectibleIcons[world]) 0 "
        counter.backgroundColor=UIColor(red:0.05,green:0.15,blue:0.38,alpha:0.85)
        counter.font=UIFont.monospacedDigitSystemFont(ofSize:16,weight:.heavy)
        counter.textColor = .white;counter.layer.cornerRadius=13;counter.clipsToBounds=true
        counter.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(counter);hud=counter
        NSLayoutConstraint.activate([counter.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:12),counter.leadingAnchor.constraint(equalTo:view.leadingAnchor,constant:14),counter.heightAnchor.constraint(equalToConstant:44)])
        gameCanvas.onHUDUpdate = { [weak counter] live in
            counter?.text=" Level \(live.displayLevel) · \(BopaviCore.collectibleIcons[world]) \(live.stars+live.coins) "
        }
        let pause=UIButton(type:.system);pause.setTitle("Ⅱ",for:.normal);pause.titleLabel?.font=UIFont.boldSystemFont(ofSize:24)
        pause.backgroundColor=UIColor(red:0.10,green:0.35,blue:0.73,alpha:1);pause.setTitleColor(.white,for:.normal);pause.layer.cornerRadius=14
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
        let s=menu("Pokušaj ponovno","\(BopaviCore.names[gameWorld]) · Level \(gameNumber)")
        label("🐦",50,.white,s)
        label("Rezultat: \(g.score()) · Prolazi: \(g.passed)/\(g.level.gates.count)",17,.white,s)
        label("\(BopaviCore.collectibles[gameWorld]): \(g.stars+g.coins) · Kovanice: \(progress.coins())",15,.white,s)
        button("▶  PONOVO",in:s){self.startGame(self.gameWorld,self.gameNumber)}
        button("OPREMA ZA KOVANICE",in:s,primary:false){self.showPerks()}
        button("MAPA SVJETOVA",in:s,primary:false){self.showWorlds()}
        button("POČETNI EKRAN",in:s,primary:false){self.showHome()}
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
        toggleRow.addArrangedSubview(l);toggleRow.addArrangedSubview(toggle);s.addArrangedSubview(toggleRow)
        toggle.addAction(UIAction{_ in self.progress.lessMotion=toggle.isOn},for:.valueChanged)
        let audio=UISwitch();audio.isOn=progress.soundEnabled
        let audioRow=UIStackView();audioRow.axis = .horizontal;audioRow.spacing=12
        let audioLabel=UILabel();audioLabel.text="Glazba i zvučni efekti";audioLabel.font=UIFont.systemFont(ofSize:16,weight:.medium);audioLabel.textColor = .white
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
