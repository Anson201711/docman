'use client';

import { useState } from 'react';
import { Input, Card, Row, Col, Typography, Empty, Spin } from 'antd';
import { SearchOutlined, FileOutlined, FolderOutlined, TagsOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'next/navigation';
import { useSearchDocuments } from '@/hooks/useDocument';

const { Title, Text } = Typography;

export default function SearchPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const [query, setQuery] = useState('');
  const { results, isLoading } = useSearchDocuments(query);

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Title level={3}>{t('search.title')}</Title>
      </div>

      <Card style={{ marginBottom: 24 }}>
        <Input
          placeholder={t('search.placeholder')}
          prefix={<SearchOutlined />}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          size="large"
          allowClear
        />
      </Card>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '40px 0' }}>
          <Spin size="large" />
          <Text style={{ display: 'block', marginTop: 16 }}>{t('search.searching')}</Text>
        </div>
      ) : query && results ? (
        <div>
          <Title level={5}>{t('search.results')} ({results.total})</Title>

          {results.total === 0 ? (
            <Empty description={t('search.noResults')} />
          ) : (
            <Row gutter={[16, 16]}>
              {results.documents?.map((doc) => (
                <Col xs={24} sm={12} md={8} lg={6} key={doc.id}>
                  <Card
                    hoverable
                    onClick={() => router.push(`/documents/${doc.id}`)}
                    cover={
                      doc.thumbnailUrl ? (
                        <img src={doc.thumbnailUrl} alt={doc.name} style={{ height: 120, objectFit: 'cover' }} />
                      ) : (
                        <div style={{ height: 120, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
                          <FileOutlined style={{ fontSize: 48, color: '#999' }} />
                        </div>
                      )
                    }
                  >
                    <Card.Meta
                      title={doc.name}
                      description={
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          {doc.type} | {new Date(doc.updatedAt).toLocaleDateString()}
                        </Text>
                      }
                    />
                  </Card>
                </Col>
              ))}

              {results.folders?.map((folder) => (
                <Col xs={24} sm={12} md={8} lg={6} key={folder.id}>
                  <Card
                    hoverable
                    onClick={() => router.push(`/documents?folder=${folder.id}`)}
                    cover={
                      <div style={{ height: 120, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#e6f7ff' }}>
                        <FolderOutlined style={{ fontSize: 48, color: '#1890ff' }} />
                      </div>
                    }
                  >
                    <Card.Meta
                      title={folder.name}
                      description={
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          {folder.documentCount} documents
                        </Text>
                      }
                    />
                  </Card>
                </Col>
              ))}
            </Row>
          )}
        </div>
      ) : (
        <Empty description={t('search.placeholder')} />
      )}
    </div>
  );
}
