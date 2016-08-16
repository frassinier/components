// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship.tdatastewardshiptaskoutput;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.datastewardship.CampaignType;
import org.talend.components.datastewardship.TdsAdvancedMappingsProperties;
import org.talend.components.datastewardship.TdsCampaignProperties;
import org.talend.components.datastewardship.TdsProperties;
import org.talend.components.datastewardship.TdsTasksMetadataProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

/**
 * {@link Properties} for Data Stewardship Task output component.
 */
public class TDataStewardshipTaskOutputProperties extends TdsProperties {

    /**
     * Campaign
     */
    public TdsCampaignProperties campaign = new TdsCampaignProperties("campaign"); //$NON-NLS-1$

    /**
     * Tasks metadata
     */
    public TdsTasksMetadataProperties tasksMetadata = new TdsTasksMetadataProperties("tasksMetadata"); //$NON-NLS-1$
    
    /**
     * Advanced Mappings Properties
     */
    public TdsAdvancedMappingsProperties advancedMappings = new TdsAdvancedMappingsProperties("advancedMappings"); //$NON-NLS-1$

    /**
     * batch size
     */
    public Property<Integer> batchSize = newInteger("batchSize", 50); //$NON-NLS-1$
    
    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TDataStewardshipTaskOutputProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(campaign.getForm(Form.MAIN));
        //mainForm.addRow(tasksMetadata.getForm(Form.MAIN));
        mainForm.addRow(batchSize);
        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(advancedMappings.getForm(Form.ADVANCED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);       
        if (form.getName().equals(Form.ADVANCED)) {
            CampaignType campaignType = campaign.campaignType.getValue();
            if (campaignType != null) {
                switch (campaignType) {
                case MERGING:
                    form.getWidget(advancedMappings.getName()).setHidden(false);
                    break;
                default:  
                    form.getWidget(advancedMappings.getName()).setHidden(true);
                    break;
                }
            }            
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.emptySet();
        }
        return Collections.singleton(MAIN_CONNECTOR);
    }

}
