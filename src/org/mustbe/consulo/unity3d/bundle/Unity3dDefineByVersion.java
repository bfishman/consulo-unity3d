/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.bundle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public enum Unity3dDefineByVersion
{
	UNITY_2_6("2.6.\\d"),
	UNITY_2_6_1("2.6.1"),
	UNITY_3_0("3.0.\\d"),
	UNITY_3_0_0("3.0.0"),
	UNITY_3_1("3.1.\\d"),
	UNITY_3_2("3.2.\\d"),
	UNITY_3_3("3.3.\\d"),
	UNITY_3_4("3.4.\\d"),
	UNITY_3_5("3.5.\\d"),
	UNITY_4_0("4.0.\\d"),
	UNITY_4_0_1("4.0.1"),
	UNITY_4_1("4.1.\\d"),
	UNITY_4_2("4.2.\\d"),
	UNITY_4_3("4.3.\\d"),
	UNITY_4_5("4.5.\\d"),
	UNITY_4_6("4.6.\\d"),
	UNITY_5("5.0.\\d"),
	UNITY_5_1("5.1.\\d"),
	UNKNOWN("\\d.\\d.\\d");

	private final Pattern myVersionPattern;

	Unity3dDefineByVersion(@RegExp String versionRegexp)
	{
		myVersionPattern = Pattern.compile(versionRegexp);
	}

	@NotNull
	public static Unity3dDefineByVersion find(String version)
	{
		for(Unity3dDefineByVersion unity3dDefineByVersion : Unity3dDefineByVersion.values())
		{
			if(unity3dDefineByVersion == UNKNOWN)
			{
				break;
			}

			Matcher matcher = unity3dDefineByVersion.myVersionPattern.matcher(version);
			if(matcher.find())
			{
				return unity3dDefineByVersion;
			}
		}
		return UNKNOWN;
	}
}
