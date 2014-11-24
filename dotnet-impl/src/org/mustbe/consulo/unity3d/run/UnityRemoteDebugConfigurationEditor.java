/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.unity3d.run;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import com.intellij.application.options.ModuleListCellRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import lombok.val;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityRemoteDebugConfigurationEditor extends SettingsEditor<UnityRemoteDebugConfiguration>
{
	private final Project myProject;

	private JComboBox myModuleComboBox;

	public UnityRemoteDebugConfigurationEditor(Project project)
	{
		myProject = project;
	}

	@Override
	protected void resetEditorFrom(UnityRemoteDebugConfiguration runConfiguration)
	{
		myModuleComboBox.setSelectedItem(runConfiguration.getConfigurationModule().getModule());
	}

	@Override
	protected void applyEditorTo(UnityRemoteDebugConfiguration runConfiguration) throws ConfigurationException
	{
		runConfiguration.getConfigurationModule().setModule((Module) myModuleComboBox.getSelectedItem());
	}

	@NotNull
	@Override
	protected JComponent createEditor()
	{
		myModuleComboBox = new JComboBox();
		myModuleComboBox.setRenderer(new ModuleListCellRenderer());
		for(val module : ModuleManager.getInstance(myProject).getModules())
		{
			if(ModuleUtilCore.getExtension(module, Unity3dModuleExtension.class) != null)
			{
				myModuleComboBox.addItem(module);
			}
		}

		FormBuilder formBuilder = FormBuilder.createFormBuilder();
		formBuilder.addLabeledComponent("Module", myModuleComboBox);
		return formBuilder.getPanel();
	}
}
