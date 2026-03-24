'use client';

import { useEffect } from 'react';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import enUS from 'antd/locale/en_US';
import '@/i18n';
import { useAppStore } from '@/stores/appStore';

const locales = {
  zh: zhCN,
  en: enUS,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { language } = useAppStore();

  return (
    <html lang={language}>
      <body>
        <ConfigProvider locale={locales[language as keyof typeof locales]}>
          {children}
        </ConfigProvider>
      </body>
    </html>
  );
}
