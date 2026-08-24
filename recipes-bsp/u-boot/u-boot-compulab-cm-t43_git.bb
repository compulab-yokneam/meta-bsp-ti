require ${COREBASE}/meta/recipes-bsp/u-boot/u-boot-common.inc
require ${COREBASE}/meta/recipes-bsp/u-boot/u-boot.inc

SUMMARY = "CompuLab U-Boot for the CM-T43 board"
HOMEPAGE = "https://github.com/compulab/u-boot"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=0507cd7da8e7ad6d6701926ec9b84c95"

DEPENDS += "bc-native"

PV = "2016.01-cm-t43-1.2"

SRC_URI = " \
    git://github.com/compulab/u-boot.git;protocol=https;branch=cm-t43/dev \
"
SRCREV = "${AUTOREV}"

UBOOT_MACHINE = "cm_t43_defconfig"
UBOOT_MAKE_TARGET = "all cm-t43-firmware"

# The u-boot-initial-env make target was introduced after this U-Boot release.
UBOOT_INITIAL_ENV = ""

do_install:append() {
    install -D -m 0644 ${B}/cm-t43-firmware ${D}/boot/cm-t43-firmware
}

do_deploy:append() {
    install -D -m 0644 ${B}/cm-t43-firmware ${DEPLOYDIR}/cm-t43-firmware
}

COMPATIBLE_MACHINE = "^cm-t43$"
