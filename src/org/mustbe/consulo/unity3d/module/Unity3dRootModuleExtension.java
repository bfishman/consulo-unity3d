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

package org.mustbe.consulo.unity3d.module;

import java.io.File;
import java.util.List;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.compiler.DotNetMacroUtil;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import org.mustbe.consulo.dotnet.module.extension.BaseDotNetSimpleModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import org.mustbe.consulo.unity3d.projectImport.Unity3dProjectUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.ide.macro.Macro;
import com.intellij.ide.macro.MacroManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dRootModuleExtension extends BaseDotNetSimpleModuleExtension<Unity3dRootModuleExtension>
{
	public static final String FILE_NAME = "$ModuleName$";

	protected Unity3dTarget myBuildTarget = Unity3dTarget.Windows;
	protected String myFileName = FILE_NAME;
	protected String myOutputDirectory = DotNetModuleExtension.DEFAULT_OUTPUT_DIR;

	public Unity3dRootModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@Override
	public void commit(@NotNull Unity3dRootModuleExtension mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);
		myBuildTarget = mutableModuleExtension.getBuildTarget();
		myFileName = mutableModuleExtension.myFileName;
		myOutputDirectory = mutableModuleExtension.myOutputDirectory;
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		super.getStateImpl(element);
		element.setAttribute("output-dir", myOutputDirectory);
		element.setAttribute("build-target", myBuildTarget.name());
		element.setAttribute("file-name", myFileName);
	}

	@Override
	protected void loadStateImpl(@NotNull Element element)
	{
		super.loadStateImpl(element);
		myFileName = element.getAttributeValue("file-name", FILE_NAME);
		myOutputDirectory = element.getAttributeValue("output-dir", DotNetModuleExtension.DEFAULT_OUTPUT_DIR);
		myBuildTarget = Unity3dTarget.valueOf(element.getAttributeValue("build-target", Unity3dTarget.Windows.name()));
	}

	@NotNull
	public String getFileName()
	{
		return StringUtil.notNullizeIfEmpty(myFileName, FILE_NAME);
	}

	@NotNull
	public String getOutputDir()
	{
		return StringUtil.notNullizeIfEmpty(myOutputDirectory, DotNetModuleExtension.DEFAULT_OUTPUT_DIR);
	}

	@NotNull
	public Unity3dTarget getBuildTarget()
	{
		return myBuildTarget;
	}

	@Override
	public boolean isSupportCompilation()
	{
		return false;
	}

	@NotNull
	@Override
	public File[] getFilesForLibraries()
	{
		Sdk sdk = getSdk();
		if(sdk == null)
		{
			return EMPTY_FILE_ARRAY;
		}

		String homePath = sdk.getHomePath();
		if(homePath == null)
		{
			return EMPTY_FILE_ARRAY;
		}

		List<String> pathsForLibraries = getPathsForLibraries(homePath, sdk);

		File[] array = EMPTY_FILE_ARRAY;
		for(String pathsForLibrary : pathsForLibraries)
		{
			File dir = new File(pathsForLibrary);
			if(dir.exists())
			{
				File[] files = dir.listFiles();
				if(files != null)
				{
					array = ArrayUtil.mergeArrays(array, files);
				}
			}
		}
		return array;
	}

	@NotNull
	private List<String> getPathsForLibraries(String homePath, @NotNull Sdk sdk)
	{
		Version version = Unity3dProjectUtil.parseVersion(sdk.getVersionString());

		List<String> list = new SmartList<String>();
		if(SystemInfo.isMac)
		{
			list.add(homePath + "/Contents/Frameworks/Mono/lib/mono/unity");
			list.add(homePath + "/Contents/Frameworks/Managed");

			// dead path?
			addUnityExtensions(list, version, homePath + "/Contents/Frameworks/UnityExtensions/Unity");
			// actual mac path
			addUnityExtensions(list, version, homePath + "/Contents/UnityExtensions/Unity");
		}
		else if(SystemInfo.isWindows || SystemInfo.isLinux)
		{
			list.add(homePath + "/Editor/Data/Mono/lib/mono/unity");
			list.add(homePath + "/Editor/Data/Managed");

			addUnityExtensions(list, version, homePath + "/Editor/Data/UnityExtensions/Unity");
		}
		return list;
	}

	private static void addUnityExtensions(List<String> list, @NotNull Version version, String baseDir)
	{
		VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(baseDir);
		if(dir == null)
		{
			return;
		}

		for(VirtualFile virtualFile : dir.getChildren())
		{
			if(virtualFile.isDirectory())
			{
				addUnityExtension(list, virtualFile, version);
			}
		}
	}

	private static void addUnityExtension(List<String> list, @NotNull VirtualFile dir, @NotNull Version version)
	{
		// UnityUI 4.6.X specific
		// {EXTENSION_NAME}/{VERSION}/{LIBRARY}

		// UnityUI 5.0 specific
		// {EXTENSION_NAME}/{LIBRARY}

		if(version.isOrGreaterThan(5, 0, 0))
		{
			list.add(dir.getPath());
			VirtualFile editorDir = dir.findChild("Editor");
			if(editorDir != null)
			{
				list.add(editorDir.getPath());
			}
		}
		else
		{
			for(VirtualFile child : dir.getChildren())
			{
				if(child.isDirectory())
				{
					list.add(child.getPath());
					VirtualFile editorDir = child.findChild("Editor");
					if(editorDir != null)
					{
						list.add(editorDir.getPath());
					}
				}
			}
		}
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return Unity3dBundleType.class;
	}

	@NotNull
	public GeneralCommandLine createDefaultCommandLine(@NotNull Sdk sdk, @Nullable DebugConnectionInfo debugConnectionInfo) throws ExecutionException
	{
		GeneralCommandLine commandLine = new GeneralCommandLine();

		String templateFilePath = getOutputDir() + "/" + myBuildTarget.getFileNameTemplate();

		try
		{
			String filePath = MacroManager.getInstance().expandSilentMarcos(templateFilePath, true, DotNetMacroUtil.createContext(getModule(),
					false));

			if(SystemInfo.isMac)
			{
				// need get app dir, like 'TestProject.app'
				String fileName = StringUtil.getShortName(filePath, '/');
				// cut '.app'
				String nameWithoutExtension = FileUtil.getNameWithoutExtension(fileName);

				commandLine.setExePath(filePath + "/Contents/MacOS/" + nameWithoutExtension);
			}
			else
			{
				commandLine.setExePath(filePath);
			}
		}
		catch(Macro.ExecutionCancelledException e)
		{
			throw new ExecutionException(e);
		}
		return commandLine;
	}
}
