echo -e 'FastAdaptiveConcMS:'
ant -Dconfig.name=FastAdaptiveConcMS -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveCopyMS:'
ant -Dconfig.name=FastAdaptiveCopyMS -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
# ant -Dconfig.name=FastAdaptiveGCTrace -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveGenCopy:'
ant -Dconfig.name=FastAdaptiveGenCopy -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveGenImmix:'
ant -Dconfig.name=FastAdaptiveGenImmix -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveGenMS:'
ant -Dconfig.name=FastAdaptiveGenMS -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveGenRC:'
ant -Dconfig.name=FastAdaptiveGenRC -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveImmix:'
ant -Dconfig.name=FastAdaptiveImmix -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveMarkCompact:'
ant -Dconfig.name=FastAdaptiveMarkCompact -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveMarkSweep:'
ant -Dconfig.name=FastAdaptiveMarkSweep -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
# ant -Dconfig.name=FastAdaptiveNoGC -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptivePoisoned:'
ant -Dconfig.name=FastAdaptivePoisoned -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveRefCount:'
ant -Dconfig.name=FastAdaptiveRefCount -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveSemiSpace:'
ant -Dconfig.name=FastAdaptiveSemiSpace -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveStickyImmix:'
ant -Dconfig.name=FastAdaptiveStickyImmix -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
echo -e '\n\nFastAdaptiveStickyMS:'
ant -Dconfig.name=FastAdaptiveStickyMS -Dhost.name=x86_64-linux -Dtarget.name=x86_64-linux -Dcp.enable.gtk-peer=--disable-gtk-peer > /dev/null
# bin/buildit --java-home /usr/lib/jvm/java-8-openjdk/ localhost FastAdaptive MarkSweep RefCount SemiSpace GenCopy GenMS CopyMS
# habanero

