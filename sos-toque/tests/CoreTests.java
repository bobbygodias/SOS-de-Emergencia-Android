package org.enterprise.sostoque;

public final class CoreTests {
    private static int checks;
    private static void ok(boolean x,String why){checks++;if(!x)throw new AssertionError(why);}
    public static void main(String[] args){
        TapSequence s=new TapSequence(5);
        for(int i=1;i<=4;i++){ok(s.tap(i*300)==i,"tap count");ok(!s.fired(),"no premature call");}
        ok(s.tap(1500)==5 && s.fired(),"fifth tap fires");ok(s.tap(1600)==5,"duplicate taps do not retrigger");
        s.reset();s.tap(0);ok(s.tap(2501)==1,"long gap resets");
        s.reset();s.tap(0);s.tap(2200);s.tap(4400);s.tap(6600);ok(s.tap(8800)==1,"whole window expires");
        s.reset();s.tap(100);ok(s.tap(50)==1,"clock regression resets");
        s=new TapSequence(3);s.tap(0);s.tap(500);ok(s.tap(1000)==3 && s.fired(),"three-tap choice");
        ok(Numbers.normalize("+55 (62) 99999-0000").equals("+5562999990000"),"normalization");
        for(String bad:new String[]{"*21*555#","tel:12345","123;456","123,456","12","++5512345678","1234567890123456","123abc","123\n*21#"})ok(Numbers.normalize(bad).isEmpty(),"reject unsafe number");
        ok(Numbers.requiresDialer("192"),"short service number uses dialer");
        ok(!Numbers.requiresDialer("+5562999990000"),"personal number allows telecom");
        ProximitySequence p=new ProximitySequence();p.update(false,0);
        for(int i=0;i<5;i++){p.update(true,100+i*900);ok(p.update(false,600+i*900)==i+1,"five distinct proximity cycles");}
        p.reset();p.update(true,0);ok(p.update(false,400)==0,"unknown starting cover not counted");
        p.update(true,1000);ok(p.update(false,1100)==1,"minimum valid cover");
        p.reset();p.update(false,0);p.update(true,100);ok(p.update(false,180)==0,"noise ignored");
        p.reset();p.update(false,0);p.update(true,100);ok(p.update(false,5000)==0,"pocket ignored");
        p.reset();p.update(false,0);p.update(true,100);p.update(false,600);p.update(true,4000);ok(p.update(false,4400)<=1,"old sequence not reused");
        p.reset();p.update(false,0);p.update(true,100);p.update(true,200);p.update(true,300);ok(p.update(false,500)==1,"repeated near is one cover");
        System.out.println("PASS: "+checks+" checks. No telephony or real contacts used.");
    }
}
