echo Booting CM-T43 from SD card ...

setenv kernel zImage
setenv bootargs console=${console} root=/dev/mmcblk0p2 rw rootfstype=ext4 rootwait

if load mmc 0:1 ${loadaddr} ${kernel}; then
	if load mmc 0:1 ${fdtaddr} ${fdtfile}; then
		bootz ${loadaddr} - ${fdtaddr}
	fi
fi

echo ERROR: CM-T43 SD-card boot failed
