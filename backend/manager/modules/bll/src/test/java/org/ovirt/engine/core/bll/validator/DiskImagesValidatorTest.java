package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

@RunWith(MockitoJUnitRunner.class)
public class DiskImagesValidatorTest {
    @ClassRule
    public static RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    private DiskImage disk1;
    private DiskImage disk2;
    private DiskImagesValidator validator;

    @Mock
    private VmDeviceDAO vmDeviceDAO;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private DiskImageDAO diskImageDao;

    @Before
    public void setUp() {
        disk1 = createDisk();
        disk1.setDiskAlias("disk1");
        disk2 = createDisk();
        disk2.setDiskAlias("disk2");
        validator = spy(new DiskImagesValidator(Arrays.asList(disk1, disk2)));
        doReturn(vmDAO).when(validator).getVmDAO();
        doReturn(vmDeviceDAO).when(validator).getVmDeviceDAO();
        doReturn(snapshotDao).when(validator).getSnapshotDAO();
        doReturn(diskImageDao).when(validator).getDiskImageDAO();
    }

    private static DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setDiskAlias(RandomUtils.instance().nextString(10));
        disk.setActive(true);
        disk.setImageStatus(ImageStatus.OK);

        return disk;
    }

    private static String createAliasReplacements(DiskImage... disks) {
        // Take the first alias
        StringBuilder msg = new StringBuilder("$diskAliases ").append(disks[0].getDiskAlias());

        // And and the rest
        for (int i = 1; i < disks.length; ++i) {
            msg.append(", ").append(disks[i].getDiskAlias());
        }

        return msg.toString();
    }

    @Test
    public void diskImagesNotIllegalBothOK() {
        assertThat("Neither disk is illegal", validator.diskImagesNotIllegal(), isValid());
    }

    @Test
    public void diskImagesNotIllegalFirstIllegal() {
        disk1.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements(hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesNotIllegalSecondtIllegal() {
        disk2.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements(hasItem(createAliasReplacements(disk2)))));
    }

    @Test
    public void diskImagesNotIllegalBothIllegal() {
        disk1.setImageStatus(ImageStatus.ILLEGAL);
        disk2.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    @Test
    public void diskImagesAlreadyExistBothExist() {
        doReturn(new DiskImage()).when(validator).getExistingDisk(any(Guid.class));
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    /**
     * Test a case when the two validated disks exists and have a null disk alias, in that case the disk aliases in
     * the CDA message should be taken from the disks existing on the setup
     */
    @Test
    public void diskImagesAlreadyDiskInImportWithNullAlias() {
        disk1.setDiskAlias(null);
        disk2.setDiskAlias(null);
        DiskImage existingImage1 = new DiskImage();
        existingImage1.setDiskAlias("existingDiskAlias1");
        DiskImage existingImage2 = new DiskImage();
        existingImage2.setDiskAlias("existingDiskAlias2");

        doReturn(existingImage1).when(validator).getExistingDisk(disk1.getId());
        doReturn(existingImage2).when(validator).getExistingDisk(disk2.getId());
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(existingImage1, existingImage2)))));
    }


    @Test
    public void diskImagesAlreadyExistOneExist() {
        doReturn(new DiskImage()).when(validator).getExistingDisk(disk1.getId());
        doReturn(null).when(validator).getExistingDisk(disk2.getId());
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesAlreadyExistBothDoesntExist() {
        doReturn(null).when(validator).getExistingDisk(any(Guid.class));
        assertEquals(validator.diskImagesAlreadyExist(), ValidationResult.VALID);
    }

    @Test
    public void diskImagesDontExist() {
        doReturn(false).when(validator).isDiskExists(disk1.getId());
        doReturn(false).when(validator).isDiskExists(disk2.getId());
        assertThat(validator.diskImagesNotExist(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_NOT_EXIST)));
    }

    @Test
    public void diskImagesExist() {
        doReturn(true).when(validator).isDiskExists(disk1.getId());
        doReturn(true).when(validator).isDiskExists(disk2.getId());
        assertEquals(validator.diskImagesNotExist(), ValidationResult.VALID);
    }

    @Test
    public void diskImagesNotLockedBothOK() {
        assertThat("Neither disk is Locked", validator.diskImagesNotLocked(), isValid());
    }

    @Test
    public void diskImagesNotLockedFirstLocked() {
        disk1.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements(hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesNotLockedSecondtLocked() {
        disk2.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements(hasItem(createAliasReplacements(disk2)))));
    }

    @Test
    public void diskImagesNotLockedBothLocked() {
        disk1.setImageStatus(ImageStatus.LOCKED);
        disk2.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    private List<VmDevice> prepareForCheckingIfDisksSnapshotsAttachedToOtherVms() {
        VmDevice device1 = createVmDeviceForDisk(disk1);
        VmDevice device2 = createVmDeviceForDisk(disk2);
        when(vmDeviceDAO.getVmDevicesByDeviceId(disk1.getId(), null)).thenReturn(Collections.singletonList(device1));
        when(vmDeviceDAO.getVmDevicesByDeviceId(disk2.getId(), null)).thenReturn(Collections.singletonList(device2));
        when(vmDAO.get(any(Guid.class))).thenReturn(new VM());
        when(snapshotDao.get(any(Guid.class))).thenReturn(new Snapshot());
        return Arrays.asList(device1, device2);
    }

    @Test
    public void diskImagesHasDerivedDisksNoStorageDomainSpecifiedSuccess() {
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        when(diskImageDao.getAllSnapshotsForParent(disk1.getImageId())).thenReturn(Collections.<DiskImage>emptyList());
        assertThat(validator.diskImagesHaveNoDerivedDisks(null),
                isValid());
    }

    @Test
    public void diskImagesHasDerivedDisksNoStorageDomainSpecifiedFailure() {
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        when(diskImageDao.getAllSnapshotsForParent(disk1.getImageId())).thenReturn(Arrays.asList(disk2));
        assertThat(validator.diskImagesHaveNoDerivedDisks(null),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_DERIVED_DISKS));
    }

    @Test
    public void diskImagesHasDerivedDisksOnStorageDomain() {
        Guid storageDomainId = Guid.Empty;
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        ArrayList<Guid> storageDomainIds = new ArrayList<>();
        storageDomainIds.add(storageDomainId);
        disk2.setStorageIds(storageDomainIds);
        when(diskImageDao.getAllSnapshotsForParent(disk1.getImageId())).thenReturn(Arrays.asList(disk2));
        assertThat(validator.diskImagesHaveNoDerivedDisks(storageDomainId),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_DERIVED_DISKS));
    }

    @Test
    public void diskImagesNoDerivedDisksOnStorageDomain() {
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        ArrayList<Guid> storageDomainIds = new ArrayList<>();
        storageDomainIds.add(Guid.newGuid());
        disk2.setStorageIds(storageDomainIds);
        when(diskImageDao.getAllSnapshotsForParent(disk1.getImageId())).thenReturn(Arrays.asList(disk2));
        assertThat(validator.diskImagesHaveNoDerivedDisks(Guid.Empty), isValid());
    }

    @Test
    public void diskImagesSnapshotsNotAttachedToOtherVmsOneDiskSnapshotAttached() {
        List<VmDevice> createdDevices = prepareForCheckingIfDisksSnapshotsAttachedToOtherVms();
        createdDevices.get(1).setSnapshotId(Guid.newGuid());
        assertThat(validator.diskImagesSnapshotsNotAttachedToOtherVms(false),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_ATTACHED_TO_ANOTHER_VM));
        verify(snapshotDao, times(1)).get(createdDevices.get(1).getSnapshotId());
        verify(snapshotDao, never()).get(createdDevices.get(0).getSnapshotId());
    }

    @Test
    public void diskImagesSnapshotsNotAttachedToOtherVmsNoDiskSnapshotsAttached() {
        List<VmDevice> createdDevices = prepareForCheckingIfDisksSnapshotsAttachedToOtherVms();
        assertThat(validator.diskImagesSnapshotsNotAttachedToOtherVms(false), isValid());
        verify(snapshotDao, never()).get(createdDevices.get(1).getSnapshotId());
        verify(snapshotDao, never()).get(createdDevices.get(0).getSnapshotId());
    }

    private VmDevice createVmDeviceForDisk(DiskImage disk) {
        VmDevice device = new VmDevice();
        device.setId(new VmDeviceId(null, disk.getId()));
        return device;
    }
}
