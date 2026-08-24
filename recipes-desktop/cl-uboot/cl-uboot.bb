SUMMARY = "CompuLab CM-T43 U-Boot SPI flash tool"
DESCRIPTION = "Install and run the CM-T43 U-Boot SPI flash update utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=258fbdf7b6336b41e0980f2046ff2ac0"
MAINTAINER = "CompuLab <compulab@compulab.com>"

SRC_URI = " \
    file://cl-uboot \
    file://cl-uboot.work \
    file://cl-uboot.quiet \
    file://cl-uboot.desktop \
    file://cl-uboot.png \
    file://COPYING \
"

S = "${UNPACKDIR}"

do_install() {
	install -d ${D}${prefix}/local/bin
	install -d ${D}${datadir}/applications
	install -m 0755 ${S}/cl-uboot ${D}${prefix}/local/bin/
	install -m 0755 ${S}/cl-uboot.work ${D}${prefix}/local/bin/
	install -m 0755 ${S}/cl-uboot.quiet ${D}${prefix}/local/bin/
	install -m 0644 ${S}/cl-uboot.png ${D}${datadir}/applications/
	install -m 0644 ${S}/cl-uboot.desktop ${D}${datadir}/applications/
}

FILES:${PN} = " \
	${prefix}/local/bin/* \
	${datadir}/applications/* \
"

RDEPENDS:${PN} = "bash dialog mtd-utils u-boot-compulab-cm-t43"

COMPATIBLE_MACHINE = "^cm-t43$"

inherit allarch
