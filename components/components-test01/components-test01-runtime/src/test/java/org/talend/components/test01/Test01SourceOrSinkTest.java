// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.test01;

import org.junit.Test;

/**
 * Created by ihor.istomin on 7/3/2017.
 */
public class Test01SourceOrSinkTest {

    @Test
    public void test1() {
        Test01Definition definition = new Test01Definition();
        Test01Properties properties = new Test01Properties();
        Test01SourceOrSink sourceOrSink = new Test01SourceOrSink();

        definition.method1();
        definition.method2();

        properties.method1();

        sourceOrSink.method1();
    }

    @Test
    public void test2() {
        Test01Definition definition = new Test01Definition();
        Test01Properties properties = new Test01Properties();
        Test01SourceOrSink sourceOrSink = new Test01SourceOrSink();

        definition.method1();
        definition.method2();

        properties.method2();

        sourceOrSink.method2();
    }
}
