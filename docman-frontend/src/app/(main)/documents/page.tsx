'use client';

import { useState } from 'react';
import { Button, Input, Space, Card, Row, Col, Upload, Modal, Form, Select, message, Typography, Dropdown, App } from 'antd';
import {
  UploadOutlined,
  PlusOutlined,
  SearchOutlined,
  GridOutlined,
  ListOutlined,
  FilterOutlined,
  SortAscendingOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'next/navigation';
import { useDocuments, useFolders, useCategories } from '@/hooks/useDocument';
import FileList from '@/components/common/FileList';
import { documentService } from '@/services/document';

const { Title } = Typography;
const { Dragger } = Upload;

export default function DocumentsPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
  const [searchQuery, setSearchQuery] = useState('');
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<string | undefined>();
  const [selectedFolder, setSelectedFolder] = useState<string | undefined>();
  const [form] = Form.useForm();

  const { documents, isLoading, mutate } = useDocuments({
    search: searchQuery,
    categoryId: selectedCategory,
    folderId: selectedFolder,
  });
  const { folders } = useFolders();
  const { categories } = useCategories();

  const handleUpload = async (values: { name?: string; categoryId?: string; folderId?: string }) => {
    const file = form.getFieldValue('file');
    if (!file) {
      message.error('Please select a file');
      return;
    }

    try {
      await documentService.uploadDocument(file, values.folderId, values.categoryId);
      message.success(t('documents.uploadSuccess'));
      setUploadModalVisible(false);
      form.resetFields();
      mutate();
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    }
  };

  const handleDownload = async (document: any) => {
    try {
      const blob = await documentService.downloadDocument(document.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = document.name;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    }
  };

  const handleDelete = async (document: any) => {
    Modal.confirm({
      title: t('documents.deleteConfirm'),
      onOk: async () => {
        try {
          await documentService.deleteDocument(document.id);
          message.success(t('common.success'));
          mutate();
        } catch (error: any) {
          message.error(error.message || t('common.error'));
        }
      },
    });
  };

  const handleRowClick = (record: any) => {
    if (record.path || record.folderId) {
      router.push(`/documents?folder=${record.id}`);
    } else {
      router.push(`/documents/${record.id}`);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0 }}>{t('documents.title')}</Title>
          </Col>
          <Col>
            <Space>
              <Button
                type="primary"
                icon={<UploadOutlined />}
                onClick={() => setUploadModalVisible(true)}
              >
                {t('documents.uploadDocument')}
              </Button>
            </Space>
          </Col>
        </Row>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Input
              placeholder={t('search.placeholder')}
              prefix={<SearchOutlined />}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              allowClear
            />
          </Col>
          <Col>
            <Space>
              <Select
                placeholder={t('folders.title')}
                style={{ width: 150 }}
                allowClear
                value={selectedFolder}
                onChange={setSelectedFolder}
                options={folders.map((f) => ({ label: f.name, value: f.id }))}
              />
              <Select
                placeholder={t('categories.title')}
                style={{ width: 150 }}
                allowClear
                value={selectedCategory}
                onChange={setSelectedCategory}
                options={categories.map((c) => ({ label: c.name, value: c.id }))}
              />
              <Button
                icon={viewMode === 'list' ? <GridOutlined /> : <ListOutlined />}
                onClick={() => setViewMode(viewMode === 'list' ? 'grid' : 'list')}
              />
            </Space>
          </Col>
        </Row>
      </Card>

      <Card>
        <FileList
          dataSource={documents}
          loading={isLoading}
          viewMode={viewMode}
          onDownload={handleDownload}
          onDelete={handleDelete}
          onRowClick={handleRowClick}
        />
      </Card>

      <Modal
        title={t('documents.uploadDocument')}
        open={uploadModalVisible}
        onCancel={() => {
          setUploadModalVisible(false);
          form.resetFields();
        }}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleUpload}>
          <Form.Item name="file" label={t('documents.title')} rules={[{ required: true }]}>
            <Dragger
              maxCount={1}
              beforeUpload={(file) => {
                form.setFieldValue('file', file);
                return false;
              }}
            >
              <p className="ant-upload-drag-icon">
                <UploadOutlined />
              </p>
              <p className="ant-upload-text">Click or drag file to this area to upload</p>
            </Dragger>
          </Form.Item>

          <Form.Item name="folderId" label={t('folders.title')}>
            <Select
              placeholder={t('folders.parentFolder')}
              options={folders.map((f) => ({ label: f.name, value: f.id }))}
            />
          </Form.Item>

          <Form.Item name="categoryId" label={t('categories.title')}>
            <Select
              placeholder={t('categories.title')}
              options={categories.map((c) => ({ label: c.name, value: c.id }))}
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setUploadModalVisible(false)}>
                {t('common.cancel')}
              </Button>
              <Button type="primary" htmlType="submit" loading={isLoading}>
                {t('common.upload')}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
