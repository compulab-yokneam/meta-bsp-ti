# CM-T43 TI Processor SDK build guide

This layer adds CompuLab CM-T43 support to TI Processor SDK Linux. It provides
the `cm-t43` machine, carries the CM-T43 Linux 6.18 patches, creates the legacy
U-Boot `bootscr.img`, and produces a compressed SD-card image and a `tar.bz2`
root filesystem archive.

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
```

The stable image links are expected to be named:

```text
tisdk-base-image-cm-t43.rootfs.wic.xz
tisdk-base-image-cm-t43.rootfs.tar.bz2
```

The `.wic.xz` file is the compressed, partitioned SD-card image. The
`.tar.bz2` file contains the root filesystem for extraction or installation by
another deployment process.

The WIC image places `bootscr.img`, `zImage`, and `am437x-sbc-t43.dtb` in its
first FAT partition. The default CM-T43 U-Boot environment loads and executes
`bootscr.img`; the script boots the root filesystem from the second SD-card
partition.

## 7. Re-enter an existing build

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
