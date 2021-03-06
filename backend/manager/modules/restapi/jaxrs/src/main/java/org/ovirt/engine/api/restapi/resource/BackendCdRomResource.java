package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.resource.DeviceResource;

public class BackendCdRomResource extends BackendDeviceResource<CdRom, CdRoms, VM> implements DeviceResource<CdRom>{

    public static final String CURRENT_CONSTRAINT_PARAMETER = "current";

    public BackendCdRomResource(Class<CdRom> modelType,
                                 Class<VM> entityType,
                                 final Guid guid,
                                 final AbstractBackendReadOnlyDevicesResource<CdRom, CdRoms, VM> collection,
                                 VdcActionType updateType,
                                 ParametersProvider<CdRom, VM> updateParametersProvider,
                                 String[] requiredUpdateFields,
                                 String... subCollections) {
        super(modelType, entityType, guid, collection, updateType, updateParametersProvider, requiredUpdateFields, subCollections);
    }

    @Override
    public CdRom update(CdRom resource) {
        // TODO: CURRENT_CONSTRAINT_PARAMETER as query parameter is depreciated and supported
        // for backward compatibility only - should be dropped at 4.0.
        if (QueryHelper.hasConstraint(getUriInfo().getQueryParameters(), CURRENT_CONSTRAINT_PARAMETER) ||
                QueryHelper.hasMatrixParam(getUriInfo(), CURRENT_CONSTRAINT_PARAMETER)) {
            validateParameters(resource, requiredUpdateFields);
            performAction(VdcActionType.ChangeDisk,
                          new ChangeDiskCommandParameters(getEntity(entityResolver, true).getId(),
                                                          resource.getFile().getId()));
            return resource;
        } else {
            return super.update(resource);
        }
    }

    @Override
    public CdRom get() {
        final boolean getCurrent = QueryHelper.hasMatrixParam(getUriInfo(), CURRENT_CONSTRAINT_PARAMETER) &&
                !"false".equalsIgnoreCase(QueryHelper.getMatrixConstraint(getUriInfo(), CURRENT_CONSTRAINT_PARAMETER));
        if (getCurrent) {
            VM vm = collection.lookupEntity(guid);
            if (vm == null) {
                return notFound();
            }
            // change the iso path so the result of 'map' will contain current cd instead of the
            // persistent configuration
            vm.setIsoPath(vm.getCurrentCd());
            return addLinks(populate(map(vm), vm));
        } else {
            return super.get();
        }
    }
}
