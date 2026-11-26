# Repackage Conveyor's precompiled payload without modifying signed binaries.
%global debug_package %{nil}
%global __os_install_post %{nil}
%global _build_id_links none
%global _binary_payload w6.gzdio

Name:           clipflow
Version:        %{app_version}
Release:        %{app_revision}
Summary:        Universal clipboard across devices
License:        AGPL-3.0-or-later
URL:            https://clipflow.com
Source0:        %{archive_name}
Source1:        linux_start.sh
Source2:        LICENSE
Source3:        clipflow.repo
Source4:        RPM-GPG-KEY-ClipFlow

# Private libraries must not satisfy dependencies of unrelated system packages.
# package_rpm.sh excludes the capabilities supplied by the bundled payload from
# Requires too, while retaining automatic detection of external system libraries.
%global __provides_exclude_from ^/usr/lib/clipflow/.*$
%global __requires_exclude %{private_requires}

Requires:       hicolor-icon-theme

%description
ClipFlow synchronizes clipboard content across devices. This package includes
the desktop application, command-line client and JetBrains Runtime.

%prep
%setup -q -c -T
tar -xzf %{SOURCE0} --strip-components=1
cp %{SOURCE2} LICENSE

%build
# The application and runtime were already built by Gradle and Conveyor.

%install
mkdir -p %{buildroot}/usr/lib/clipflow %{buildroot}/usr/bin %{buildroot}/usr/share
cp -a bin lib %{buildroot}/usr/lib/clipflow/
# EL9's classifier and elfdeps both require an execute bit to emit Requires.
# Normalize library modes without changing their precompiled binary contents.
find %{buildroot}/usr/lib/clipflow -type f -name '*.so*' -exec chmod a+x {} +
cp -a share/. %{buildroot}/usr/share/
# root-inputs are omitted from Conveyor tarballs; retain the GUI restart helper.
install -m 0755 %{SOURCE1} %{buildroot}/usr/lib/clipflow/bin/start.sh
ln -s /usr/lib/clipflow/lib/app/bin/clipflow-cli %{buildroot}/usr/bin/clipflow
# Subscribe the installation to the dnf repository so `dnf upgrade` picks up
# new releases; the public key verifies both the packages and the repodata.
install -D -m 0644 %{SOURCE3} %{buildroot}/etc/yum.repos.d/clipflow.repo
install -D -m 0644 %{SOURCE4} %{buildroot}/etc/pki/rpm-gpg/RPM-GPG-KEY-ClipFlow

%check
test -x %{buildroot}/usr/lib/clipflow/bin/clipflow
test -x %{buildroot}/usr/lib/clipflow/lib/app/bin/clipflow-cli
test -f %{buildroot}/usr/share/applications/com.clipflow.desktop
test -f %{buildroot}/usr/share/metainfo/com.clipflow.metainfo.xml
# Check actual ELF headers as well as the RPM target; no foreign binary runs.
case '%{_target_cpu}' in
    x86_64) expected_machine='Advanced Micro Devices X86-64' ;;
    aarch64) expected_machine='AArch64' ;;
    *) exit 1 ;;
esac
for executable in bin/clipflow lib/app/bin/clipflow-cli lib/runtime/lib/server/libjvm.so; do
    LC_ALL=C readelf -h "%{buildroot}/usr/lib/clipflow/$executable" | grep -F "$expected_machine"
done

%files
%license LICENSE
%config(noreplace) /etc/yum.repos.d/clipflow.repo
/etc/pki/rpm-gpg/RPM-GPG-KEY-ClipFlow
/usr/bin/clipflow
/usr/lib/clipflow/
/usr/share/applications/com.clipflow.desktop
/usr/share/metainfo/com.clipflow.metainfo.xml
/usr/share/icons/hicolor/*/apps/clipflow.png
