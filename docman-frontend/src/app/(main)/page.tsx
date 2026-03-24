'use client';

import { Row, Col, Card, Statistic, List, Avatar, Typography, Space, Button, theme } from 'antd';
import {
  FileOutlined,
  FolderOutlined,
  UploadOutlined,
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'next/navigation';
import { useRecentDocuments } from '@/hooks/useDocument';

const { Title, Text } = Typography;

export default function DashboardPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { documents: recentDocuments, isLoading } = useRecentDocuments(5);
  const { token } = theme.useToken();

  const statistics = [
    {
      title: t('dashboard.totalDocuments'),
      value: 128,
      prefix: <FileOutlined style={{ color: token.colorPrimary }} />,
    },
    {
      title: t('dashboard.totalFolders'),
      value: 24,
      prefix: <FolderOutlined style={{ color: token.colorPrimary }} />,
    },
    {
      title: t('dashboard.totalSize'),
      value: '2.5 GB',
      prefix: <EditOutlined style={{ color: token.colorPrimary }} />,
    },
  ];

  const quickActions = [
    {
      title: t('documents.uploadDocument'),
      icon: <UploadOutlined />,
      onClick: () => router.push('/documents?action=upload'),
    },
    {
      title: t('folders.createFolder'),
      icon: <PlusOutlined />,
      onClick: () => router.push('/folders?action=create'),
    },
    {
      title: t('search.title'),
      icon: <SearchOutlined />,
      onClick: () => router.push('/search'),
    },
  ];

  return (
    <div>
      <Title level={3}>{t('dashboard.title')}</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {statistics.map((stat, index) => (
          <Col xs={24} sm={12} md={8} key={index}>
            <Card>
              <Statistic title={stat.title} value={stat.value} prefix={stat.prefix} />
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card
            title={t('dashboard.recentDocuments')}
            extra={<Button type="link" onClick={() => router.push('/documents')}>{t('common.search')} &gt;</Button>}
          >
            <List
              loading={isLoading}
              dataSource={recentDocuments}
              renderItem={(item) => (
                <List.Item
                  actions={[
                    <Button key="view" type="text" size="small" icon={<FileOutlined />} onClick={() => router.push(`/documents/${item.id}`)} />,
                  ]}
                >
                  <List.Item.Meta
                    avatar={<Avatar icon={<FileOutlined />} style={{ backgroundColor: token.colorPrimary }} />}
                    title={<a onClick={() => router.push(`/documents/${item.id}`)}>{item.name}</a>}
                    description={new Date(item.updatedAt).toLocaleDateString()}
                  />
                </List.Item>
              )}
              locale={{ emptyText: t('documents.noDocuments') }}
            />
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title={t('dashboard.quickActions')}>
            <Space direction="vertical" style={{ width: '100%' }} size="middle">
              {quickActions.map((action, index) => (
                <Button
                  key={index}
                  icon={action.icon}
                  size="large"
                  block
                  onClick={action.onClick}
                  style={{ textAlign: 'left' }}
                >
                  {action.title}
                </Button>
              ))}
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
