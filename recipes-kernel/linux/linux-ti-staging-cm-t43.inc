FILESEXTRAPATHS:prepend := "${THISDIR}/linux-ti-staging-6.18:"

KERNEL_DEVICETREE:cm-t43 = " \
    ti/omap/am437x-cm-t43.dtb \
    ti/omap/am437x-cm-t43-legacy.dtb \
    ti/omap/am437x-sbc-t43.dtb \
    ti/omap/am437x-sbc-t43-legacy.dtb \
"

do_deploy:append:cm-t43() {
    ln -sf zImage-${KERNEL_IMAGE_NAME}${KERNEL_IMAGE_BIN_EXT} \
        ${DEPLOYDIR}/zImage-cm-t43
}

SRC_URI:append:cm-t43:bsp-ti-6_18 = " \
    file://0001-net-ethernet-ti-add-CM-T43-PHY-initialization.patch \
    file://0002-ARM-dts-am437x-cm-t43-support-RTL8211E-PHYs.patch \
    file://0003-ARM-configs-enable-CM-T43-RTL8211E-support.patch \
    file://0004-ARM-dts-am437x-sbc-t43-fix-SD-card-detect-polarity.patch \
    file://0005-ARM-dts-am437x-cm-t43-add-legacy-Atheros-PHY-variant.patch \
"
