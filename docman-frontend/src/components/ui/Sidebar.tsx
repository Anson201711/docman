'use client';

import { Layout, Menu, theme } from 'antd';
import {
  HomeOutlined,
  FileOutlined,
  FolderOutlined,
  SearchOutlined,
  TagsOutlined,
  SettingOutlined,
  ClockCircleOutlined,
  StarOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter, usePathname } from 'next/navigation';

const { Sider } = Layout;

export default function Sidebar() {
  const { t } = useTranslation();
  const router = useRouter();
  const pathname = usePathname();
  const { token } = theme.useToken();

  const mainMenuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: t('menu.home'),
    },
    {
      key: '/documents',
      icon: <FileOutlined />,
      label: t('menu.documents'),
    },
    {
      key: '/folders',
      icon: <FolderOutlined />,
      label: t('menu.folders'),
    },
    {
      key: '/categories',
      icon: <TagsOutlined />,
      label: t('menu.categories'),
    },
    {
      key: '/search',
      icon: <SearchOutlined />,
      label: t('menu.search'),
    },
  ];

  const secondaryMenuItems = [
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: t('menu.settings'),
    },
  ];

  const selectedKey = mainMenuItems.find((item) => pathname.startsWith(item.key))?.key || '/';

  return (
    <Sider
      width={220}
      style={{
        background: token.colorBgContainer,
        borderRight: `1px solid ${token.colorBorderSecondary}`,
      }}
    >
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <div style={{ flex: 1, padding: '16px 8px' }}>
          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            style={{ border: 'none' }}
            items={mainMenuItems}
            onClick={({ key }) => router.push(key)}
          />
        </div>

        <div style={{ padding: '16px 8px', borderTop: `1px solid ${token.colorBorderSecondary}` }}>
          <Menu
            mode="inline"
            selectedKeys={[pathname]}
            style={{ border: 'none' }}
            items={secondaryMenuItems}
            onClick={({ key }) => router.push(key)}
          />
        </div>
      </div>
    </Sider>
  );
}
