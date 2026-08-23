SUMMARY = "U-Boot SD-card boot script for CompuLab CM-T43"
DESCRIPTION = "Creates the legacy bootscr.img consumed by the default CM-T43 U-Boot environment."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "^cm-t43$"
PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "u-boot-mkimage-native"
INHIBIT_DEFAULT_DEPS = "1"

SRC_URI = "file://bootscr.cmd"

S = "${UNPACKDIR}"

inherit deploy nopackages

do_compile() {
	install -d ${B}
	uboot-mkimage -A arm -T script -C none \
		-n "CM-T43 SD-card boot" \
		-d ${UNPACKDIR}/bootscr.cmd ${B}/bootscr.img
}

do_deploy() {
	install -d ${DEPLOYDIR}
	install -m 0644 ${B}/bootscr.img ${DEPLOYDIR}/bootscr.img
}

addtask deploy after do_compile before do_build
