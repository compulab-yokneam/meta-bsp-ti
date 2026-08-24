# CM-T43 TI Processor SDK build guide

This layer adds CompuLab CM-T43 support to TI Processor SDK Linux. It provides
the `cm-t43` machine, carries the CM-T43 Linux 6.18 patches, builds CompuLab's
CM-T43 U-Boot and the legacy `bootscr.img`, and produces a compressed SD-card
image and a `tar.bz2` root filesystem archive.

The layer is intended for the following configuration:

- TI Processor SDK 12.01
- Yocto Project release series: Wrynose
- TI BSP: Linux 6.18
- Machine: `cm-t43`
- Distribution: `arago`

## 1. Host prerequisites

Prepare a Linux build host that satisfies the TI Processor SDK and Yocto host
requirements. The host must have Git, Python 3, the standard compilation tools,
and enough free disk space for a full SDK build. The SDK downloads and build
output can consume tens of gigabytes.

Do not run BitBake as `root`.

## 2. Set up the TI Processor SDK sources

Skip this section if the TI SDK source tree and its `build` directory have
already been created.

Choose a location for the SDK:

```bash
export TISDK_ROOT=/path/to/tisdk
```

Clone TI's `oe-layersetup` repository:

```bash
git clone https://git.ti.com/git/arago-project/oe-layersetup.git "${TISDK_ROOT}"
cd "${TISDK_ROOT}"
```

Set up the Processor SDK 12.01 Wrynose repositories at their pinned revisions:

```bash
./oe-layertool-setup.sh \
    -f configs/processor-sdk/processor-sdk-wrynose-12.01.00.05.03-config.txt
```

After the command completes, the SDK should contain at least these paths:

```text
tisdk/
├── build/
│   └── conf/
│       ├── bblayers.conf
│       ├── local.conf
│       └── setenv
└── sources/
    ├── bitbake/
    ├── meta-ti/
    ├── meta-tisdk/
    └── oe-core/
```

## 3. Install `meta-bsp-ti`

Place this complete layer under the SDK's `sources` directory. Use either a
Git clone, if a repository URL is available, or copy an existing checkout.

Git method:

```bash
git clone <meta-bsp-ti-repository-url> \
    "${TISDK_ROOT}/sources/meta-bsp-ti"
```

Copy method:

```bash
cp -a /path/to/meta-bsp-ti "${TISDK_ROOT}/sources/meta-bsp-ti"
```

Verify that the layer configuration and CM-T43 machine exist:

```bash
test -f "${TISDK_ROOT}/sources/meta-bsp-ti/conf/layer.conf"
test -f "${TISDK_ROOT}/sources/meta-bsp-ti/conf/machine/cm-t43.conf"
```

## 4. Set up the build environment

Start a new shell, enter the generated build directory, and source its
environment file:

```bash
cd "${TISDK_ROOT}/build"
source conf/setenv
export MACHINE=cm-t43
export DISTRO=arago
```

The environment must be sourced again in every new shell used for BitBake.

Add `meta-bsp-ti` to the build:

```bash
bitbake-layers add-layer "${TISDK_ROOT}/sources/meta-bsp-ti"
```

If the layer is already present in `conf/bblayers.conf`, do not add it a second
time.

Verify the layer and machine registration:

```bash
bitbake-layers show-layers | grep meta-bsp-ti
bitbake-layers show-machines | grep cm-t43
```

Expected results include:

```text
bsp-ti    .../sources/meta-bsp-ti
cm-t43 (meta-bsp-ti)
```

Confirm the selected machine and image formats:

```bash
bitbake-getvar MACHINE
bitbake-getvar IMAGE_FSTYPES
bitbake-getvar WKS_FILE
```

The important resolved values are:

```text
MACHINE="cm-t43"
IMAGE_FSTYPES="wic.xz tar.bz2 ..."
WKS_FILE="sdimage-2part.wks"
```

The Arago image recipes can append other formats, such as `cpio.xz`; this does
not replace the required `wic.xz` and `tar.bz2` outputs.

## 5. Build `tisdk-base-image`

From the initialized build shell, run:

```bash
bitbake tisdk-base-image
```

The first build downloads and compiles the complete dependency graph and can
take considerable time. Later builds reuse the downloads and shared-state
cache.

To validate only the CM-T43 kernel patch application before a full image build,
run:

```bash
bitbake -c patch linux-ti-staging
```

To build only the U-Boot SD-card script, run:

```bash
bitbake cm-t43-bootscript
```

To build only the CM-T43 U-Boot (`MLO` and `u-boot.img`), run:

```bash
bitbake u-boot-compulab-cm-t43
```

The recipe uses `${AUTOREV}` with CompuLab's `cm-t43/dev` branch, so each build
fetches and uses the latest commit available on that branch.

To perform a dry run of the complete image task graph without executing the
pending tasks, run:

```bash
bitbake -n tisdk-base-image
```

## 6. Locate the build artifacts

Successful images are deployed under:

```text
${TISDK_ROOT}/build/deploy-ti/images/cm-t43/
```

List the required artifacts with:

```bash
ls -lh "${TISDK_ROOT}/build/deploy-ti/images/cm-t43/"*wic.xz
ls -lh "${TISDK_ROOT}/build/deploy-ti/images/cm-t43/"*tar.bz2
ls -lh "${TISDK_ROOT}/build/deploy-ti/images/cm-t43/bootscr.img"
ls -lh "${TISDK_ROOT}/build/deploy-ti/images/cm-t43/zImage-cm-t43"
ls -lh "${TISDK_ROOT}/build/deploy-ti/images/cm-t43/cm-t43-firmware"
ls -lh "${TISDK_ROOT}/build/deploy-ti/images/cm-t43/MLO"
ls -lh "${TISDK_ROOT}/build/deploy-ti/images/cm-t43/u-boot.img"
```

The stable image links are expected to be named:

```text
tisdk-base-image-cm-t43.rootfs.wic.xz
tisdk-base-image-cm-t43.rootfs.tar.bz2
```

The `.wic.xz` file is the compressed, partitioned SD-card image. The
`.tar.bz2` file contains the root filesystem for extraction or installation by
another deployment process.

The WIC image places `MLO`, `u-boot.img`, `bootscr.img`, `zImage-cm-t43`,
`cm-t43-firmware`, and `am437x-sbc-t43.dtb` in its first FAT partition. The
default CM-T43 U-Boot environment loads and executes `bootscr.img`; the script
boots the root filesystem from the second SD-card partition.

## 7. Download and deploy a ready-to-run image

A prebuilt CM-T43 image is available from the
[CM-T43 image folder](https://drive.google.com/drive/folders/16fkJonArmD3tlDMUipygQmDkWi7fupV_).
Download these files with a web browser:

```text
tisdk-base-image-cm-t43.rootfs-<time-stamp>.wic.xz
tisdk-base-image-cm-t43.rootfs-<time-stamp>.manifest
```

The `.wic.xz` file is the compressed, ready-to-write SD-card image. The
`.manifest` file lists the packages installed in that image and is not required
for writing or booting the SD card.

On a Linux host, enter the directory containing the downloaded files and check
the compressed image before writing it:

```bash
cd /path/to/download-directory
IMAGE='tisdk-base-image-cm-t43.rootfs-<time-stamp>.wic.xz'
xz -t "${IMAGE}"
```

Insert the SD card and identify its whole-device path. Compare the output before
and after inserting the card if the target is not immediately clear:

```bash
lsblk -p -o NAME,SIZE,MODEL,TRAN,MOUNTPOINTS
```

Set `SD_DEVICE` to the whole SD-card device, such as `/dev/sdX` or
`/dev/mmcblkN`, not to a partition such as `/dev/sdX1` or `/dev/mmcblkNp1`:

```bash
SD_DEVICE=/dev/sdX
lsblk -p -o NAME,SIZE,MODEL,TRAN,MOUNTPOINTS "${SD_DEVICE}"
```

This operation overwrites the selected device completely. Verify `SD_DEVICE`
carefully and unmount every mounted partition shown beneath it before
continuing. Then write the compressed image directly to the card:

```bash
set -o pipefail
xz -dc -- "${IMAGE}" | sudo dd of="${SD_DEVICE}" \
    bs=4M iflag=fullblock conv=fsync status=progress
sync
```

### Boot the CM-T43 from the SD card

The CM-T43 normally uses its on-board SPI flash as the primary boot storage.
The `ALT.BOOT` button changes the boot-mode selection sampled by the AM437x
BootROM during reset, selecting the SD card as the source for the complete
U-Boot bootloader chain instead of the SPI flash.

To perform an SD-card boot:

1. Power off the CM-T43 system and insert the prepared SD card into the P9
   socket on the SB-SOM-T43 base board.
2. Press and hold the `ALT.BOOT` (`SW3`) button.
3. While holding `ALT.BOOT`, power on or reset the system.
4. Release the button after the board starts booting from the SD card.

The BootROM loads `MLO` from the first FAT partition, and `MLO` starts
`u-boot.img`. U-Boot then executes `bootscr.img`, which loads the CM-T43 kernel
and boots the root filesystem from the image's second partition.

### Deploy the running SD-card image to the internal eMMC

After the CM-T43 has booted successfully from the prepared SD card,
`cl-deploy` can copy that installation to the module's internal eMMC device.
The CM-T43 image includes this tool by default.

Log in as `root` and verify that the system is running from the SD card and that
the internal eMMC is `/dev/mmcblk1`:

```bash
findmnt -no SOURCE /
lsblk -p -o NAME,SIZE,MODEL,TYPE,MOUNTPOINTS
```

The following command destroys all existing partitions and data on
`/dev/mmcblk1`. Run it only after confirming that `/dev/mmcblk1` is the internal
eMMC and is not the device that provides the currently running root filesystem:

```bash
DST=/dev/mmcblk1 cl-deploy
```

Review the source and destination displayed by `cl-deploy`, then confirm the
operation when prompted. The tool recreates the SD-card partition layout and
copies the boot and root filesystems to the eMMC.

After deployment completes successfully, power off the board, remove the SD
card, and power on the board without holding `ALT.BOOT` to verify that the system
boots from the internal eMMC.

## 8. Re-enter an existing build

For subsequent build sessions, only the environment initialization and build
command are required:

```bash
export TISDK_ROOT=/path/to/tisdk
cd "${TISDK_ROOT}/build"
source conf/setenv
export MACHINE=cm-t43
export DISTRO=arago
bitbake tisdk-base-image
```

## Troubleshooting

### `Layer meta-bsp-ti not found`

Check that the layer exists under `sources`, then add it again:

```bash
test -f "${TISDK_ROOT}/sources/meta-bsp-ti/conf/layer.conf"
bitbake-layers add-layer "${TISDK_ROOT}/sources/meta-bsp-ti"
```

### `Unable to find conf/machine/cm-t43.conf`

Confirm that `MACHINE` is exported and that BitBake sees the layer:

```bash
export MACHINE=cm-t43
bitbake-layers show-machines | grep cm-t43
```

### Kernel patches are not selected

The CM-T43 patches are intentionally scoped to `MACHINE=cm-t43` and the TI
6.18 BSP. Check the resolved kernel source list:

```bash
bitbake-getvar -r linux-ti-staging SRC_URI
```

The result should contain all four CM-T43 patch filenames from this layer.

### Start from another shell

If commands such as `bitbake` or `bitbake-layers` are not found, return to the
build directory and source `conf/setenv` again.
